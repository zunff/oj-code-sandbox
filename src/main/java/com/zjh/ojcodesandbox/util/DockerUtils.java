package com.zjh.ojcodesandbox.util;

import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.zjh.ojcodesandbox.model.ExecuteMessage;
import com.zjh.ojcodesandbox.model.enums.ExecuteCodeStatusEum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.io.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
public class DockerUtils {

    public static final String JAVA_IMAGE = "openjdk:8-alpine";

    /**
     * 建立连接
     */
    public static DockerClient connectDockerClient() {
        return DockerClientBuilder.getInstance().build();
    }

    /**
     * 拉取镜像
     *
     * @param client
     * @return 返回拉取镜像的名称
     */
    public static String pullJavaImage(DockerClient client) {
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                log.info("下载镜像：" + item.getStatus());
                super.onNext(item);
            }
        };
        try {
            client.pullImageCmd(JAVA_IMAGE).exec(pullImageResultCallback).awaitCompletion();
        } catch (InterruptedException e) {
            log.error("拉取镜像异常");
            e.printStackTrace();
        }
        log.info("下载镜像" + JAVA_IMAGE + "成功");
        return JAVA_IMAGE;
    }


    /**
     * 创建可交互容器
     *
     * @param client
     * @param source 本机文件目录
     * @param desc   容器中的文件目录
     * @return 容器ID
     */
    public static String createInterContainerWithBind(DockerClient client, String source, String desc) {
        CreateContainerCmd containerCmd = client.createContainerCmd(JAVA_IMAGE);
        HostConfig hostConfig = new HostConfig();
        hostConfig.setBinds(new Bind(source, new Volume(desc)));
        hostConfig.withMemory(100 * 1000 * 1000L); //100m
        hostConfig.withCpuCount(1L); //容器只能占用一个CPU
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true)//设置网络配置为关闭
                .withAttachStdin(true) //下面几个都是开启容器交互
                .withReadonlyRootfs(true)//禁止往根目录写文件
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();
        return createContainerResponse.getId();
    }

    /**
     * 启动容器
     *
     * @param client
     * @param containId
     */
    public static void startContainer(DockerClient client, String containId) {
        client.startContainerCmd(containId).exec();
    }

    public static ExecuteMessage execInputByArgs(DockerClient client, String containerId, String[] cmd, Long timeout) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        ExecCreateCmdResponse execCreateCmdResponse = client.execCreateCmd(containerId)
                .withCmd(cmd)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .exec();
        log.info("创建执行指令：" + execCreateCmdResponse);

        String execId = execCreateCmdResponse.getId();

        final String[] message = {null};
        final String[] errMessage = {null};
        final Boolean[] isTimeout = {true};

        ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
            @Override
            public void onNext(Frame frame) {
                if (StreamType.STDERR.equals(frame.getStreamType())) {
                    String result = new String(frame.getPayload());
                    log.error("输出错误结果：" + result);
                    errMessage[0] = result;
                } else {
                    byte[] bytes = frame.getPayload();
                    int len = bytes.length;
                    if (len > 0 && bytes[len - 1] == '\n') {
                        len--;
                    }
                    String result = new String(bytes, 0, len);
                    log.info("输出结果：" + result);
                    message[0] = result;
                }
                super.onNext(frame);
            }

            @Override
            public void onComplete() {
                //如果正常完成了程序，而不是超时被掐掉，就会执行这个方法
                //正常完成程序，设置为未超时
                isTimeout[0] = false;
                super.onComplete();
            }
        };
        StopWatch stopWatch = new StopWatch();
        //获取占用的内存
        final long[] memory = {0L};
        StatsCmd statsCmd = client.statsCmd(containerId);
        statsCmd.exec(new ResultCallback<Statistics>() {
            @Override
            public void onNext(Statistics statistics) {
                Long m = statistics.getMemoryStats().getUsage();

                log.info("内存占用：" + m);
                memory[0] = Math.max(memory[0], m);
            }

            @Override
            public void onStart(Closeable closeable) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void close() throws IOException {

            }
        });
        try {
            stopWatch.start();
            client.execStartCmd(execId).exec(execStartResultCallback).awaitCompletion(timeout, TimeUnit.MILLISECONDS);
            stopWatch.stop();
            statsCmd.close();
        } catch (InterruptedException e) {
            log.error("程序执行异常");
            return ErrorUtils.getExecuteMessage(e);
        }
        executeMessage.setErrorMessage(errMessage[0]);
        executeMessage.setMessage(message[0]);
        executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        executeMessage.setMemory(memory[0]);
        //先标记为成功
        executeMessage.setStatus(ExecuteCodeStatusEum.SUCCESS.getValue());
        //如果有错误再换状态码
        if (isTimeout[0]) {
            executeMessage.setStatus(ExecuteCodeStatusEum.TIMEOUT_FAIL.getValue());
            executeMessage.setErrorMessage(ExecuteCodeStatusEum.TIMEOUT_FAIL.getText());
        } else if (StrUtil.isNotBlank(errMessage[0])) {
            //执行失败，记录错误，便于返回给用户
            executeMessage.setStatus(ExecuteCodeStatusEum.RUNNING_FAIL.getValue());
        }
        return executeMessage;
    }

    /**
     * 删除容器
     *
     * @param client
     * @param containerId
     */
    public static void removeContainer(DockerClient client, String containerId, Boolean force) {
        client.removeContainerCmd(containerId).withForce(force).exec();
    }
}
