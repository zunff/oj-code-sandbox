package com.zjh.ojcodesandbox.impl;

import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.zjh.ojcodesandbox.model.ExecuteCodeResponse;
import com.zjh.ojcodesandbox.model.ExecuteMessage;
import com.zjh.ojcodesandbox.model.JudgeInfo;
import com.zjh.ojcodesandbox.model.enums.ExecuteCodeStatusEum;
import com.zjh.ojcodesandbox.template.JavaCodeSandboxTemplate;
import com.zjh.ojcodesandbox.util.DockerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class JavaDockerCodeArgsSandbox extends JavaCodeSandboxTemplate {
    public static final Long TIME_OUT = 5000L;

    public static Boolean FIRST_INIT = true;

    /**
     * Docker连接
     */
    private DockerClient client = null;

    /**
     * 创建的容器编号
     */
    private String containerId = "";

    @Override
    public List<ExecuteMessage> execCodeToMessageList(List<String> inputList, String userCodeParentPath) {
        client = DockerUtils.connectDockerClient();
        if (FIRST_INIT) {
            DockerUtils.pullJavaImage(client);
            FIRST_INIT = false;
        }
        containerId = DockerUtils.createInterContainerWithBind(client, userCodeParentPath, "/app");
        DockerUtils.startContainer(client, containerId);

        //执行
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String input : inputList) {
            String[] s = input.split(" ");
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, s);
            ExecuteMessage executeMessage = DockerUtils.execInputByArgs(client, containerId, cmdArray, TIME_OUT);
            executeMessageList.add(executeMessage);
        }
        return executeMessageList;
    }


    @Override
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        //先标记为成功，如果判断到有案例执行报错，再改为失败
        executeCodeResponse.setStatus(ExecuteCodeStatusEum.SUCCESS.getValue());
        //运行时间取所有用例中的最大值
        Long maxTime = 0L;
        Long maxMemory = 0L;
        for (ExecuteMessage executeMessage : executeMessageList) {
            if (!executeMessage.getStatus().equals(ExecuteCodeStatusEum.SUCCESS.getValue())) {
                //执行失败
                executeCodeResponse.setStatus(executeMessage.getStatus());
                executeCodeResponse.setMessage(executeMessage.getErrorMessage());
                //执行错误存个error串展位
                outputList.add("error");
                //跳过本返回
                continue;
            }
            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
            Long memory = executeMessage.getMemory();
            if (memory != null) {
                maxMemory = Math.max(maxMemory, memory);
            }
            outputList.add(executeMessage.getMessage());
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        judgeInfo.setMemory(maxMemory);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    @Override
    public void delTemp(File userCodeFile) {
        //删除本地文件
        super.delTemp(userCodeFile);
        //删除创建的Docker容器
        DockerUtils.removeContainer(client, containerId, true);
    }
}
