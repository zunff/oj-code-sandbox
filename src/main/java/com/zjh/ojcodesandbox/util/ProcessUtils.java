package com.zjh.ojcodesandbox.util;

import cn.hutool.core.util.StrUtil;
import com.zjh.ojcodesandbox.model.ExecuteMessage;
import com.zjh.ojcodesandbox.model.enums.ExecuteCodeStatusEum;
import org.springframework.util.StopWatch;

import java.io.*;

public class ProcessUtils {


    public static final int TIME_OUT = 5000;

    /**
     * 执行交互式进程把那个获取信息
     * @param cmd
     * @param input
     * @return
     */
    public static ExecuteMessage runProcessInputScanner(String cmd, String input) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        //先设置为执行成功
        executeMessage.setStatus(ExecuteCodeStatusEum.SUCCESS.getValue());

        BufferedReader bufferedReader = null;
        BufferedReader infoBufferedReader = null;
        BufferedReader errorBufferedReader = null;
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            Process process = Runtime.getRuntime().exec(cmd);
            //将测试用例写入输入流
            OutputStream outputStream = process.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            String join = StrUtil.join("\n", input.split(" ")) + "\n";
            outputStreamWriter.write(join);
            outputStreamWriter.flush();

            new Thread(() -> {
                try {
                    Thread.sleep(TIME_OUT);
                    process.destroy();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            int exitValue = process.waitFor();
            executeMessage.setExitValue(exitValue);
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
            if (exitValue == 0) {
                //读取程序执行后的输出信息
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                executeMessage.setMessage(sb.toString());
            } else {
                //读取程序执行后的输出信息
                infoBufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder infoSb = new StringBuilder();
                String infoLine;
                while ((infoLine = infoBufferedReader.readLine()) != null) {
                    infoSb.append(infoLine);
                }
                executeMessage.setMessage(infoSb.toString());
                //读取程序执行后的报错信息
                errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorSb = new StringBuilder();
                String errorLine;
                while ((errorLine = errorBufferedReader.readLine()) != null) {
                    errorSb.append(errorLine);
                }
                executeMessage.setMessage(errorSb.toString());
                executeMessage.setStatus(ExecuteCodeStatusEum.RUNNING_FAIL.getValue());
            }
        } catch (Exception e) {
            return ErrorUtils.getExecuteMessage(e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    return ErrorUtils.getExecuteMessage(e);
                }
            }
            if (infoBufferedReader != null) {
                try {
                    infoBufferedReader.close();
                } catch (IOException e) {
                    return ErrorUtils.getExecuteMessage(e);
                }
            }
            if (errorBufferedReader != null) {
                try {
                    errorBufferedReader.close();
                } catch (IOException e) {
                    return ErrorUtils.getExecuteMessage(e);
                }
            }
        }

        return executeMessage;
    }

    /**
     * 编译或执行args输入参数的代码
     * @param cmd
     * @param option 操作 0为编译 1为运行
     * @return
     */
    public static ExecuteMessage runProcessInputArgs(String cmd, Integer option) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        //先设置为执行成功
        executeMessage.setStatus(ExecuteCodeStatusEum.SUCCESS.getValue());

        BufferedReader bufferedReader = null;
        BufferedReader infoBufferedReader = null;
        BufferedReader errorBufferedReader = null;
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            Process compileProcess = Runtime.getRuntime().exec(cmd);

            new Thread(() -> {
                try {
                    Thread.sleep(TIME_OUT);
                    compileProcess.destroy();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            int exitValue = compileProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
            if (exitValue == 0) {
                //读取程序执行后的输出信息
                bufferedReader = new BufferedReader(new InputStreamReader(compileProcess.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                executeMessage.setMessage(sb.toString());
            } else {
                //读取程序执行后的输出信息
                infoBufferedReader = new BufferedReader(new InputStreamReader(compileProcess.getInputStream()));
                StringBuilder infoSb = new StringBuilder();
                String infoLine;
                while ((infoLine = infoBufferedReader.readLine()) != null) {
                    infoSb.append(infoLine);
                }
                executeMessage.setMessage(infoSb.toString());
                //读取程序执行后的报错信息
                errorBufferedReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
                StringBuilder errorSb = new StringBuilder();
                String errorLine;
                while ((errorLine = errorBufferedReader.readLine()) != null) {
                    errorSb.append(errorLine);
                }
                executeMessage.setMessage(errorSb.toString());
                executeMessage.setStatus(option == 0 ? ExecuteCodeStatusEum.COMPILE_FAIL.getValue() : ExecuteCodeStatusEum.RUNNING_FAIL.getValue());
            }
        } catch (Exception e) {
            return ErrorUtils.getExecuteMessage(e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    return ErrorUtils.getExecuteMessage(e);
                }
            }
            if (infoBufferedReader != null) {
                try {
                    infoBufferedReader.close();
                } catch (IOException e) {
                    return ErrorUtils.getExecuteMessage(e);
                }
            }
            if (errorBufferedReader != null) {
                try {
                    errorBufferedReader.close();
                } catch (IOException e) {
                    return ErrorUtils.getExecuteMessage(e);
                }
            }
        }

        return executeMessage;
    }
}
