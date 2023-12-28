package com.zjh.ojcodesandbox.util;

import com.zjh.ojcodesandbox.model.ExecuteMessage;
import com.zjh.ojcodesandbox.model.enums.ExecuteCodeStatusEum;
import org.springframework.util.StopWatch;

import java.io.*;

public class ProcessUtils {

    /**
     * 执行代码，从控制台Scanner输入参数
     * @param runCmd 运行的指令
     * @param input 需要在控制台输入的参数，空格隔开
     * @return
     */
    public static ExecuteMessage runProcessInputScanner(String runCmd, String input) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        //先设置为执行成功
        executeMessage.setStatus(ExecuteCodeStatusEum.SUCCESS.getValue());
        BufferedReader errorBufferedReader = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            Process process = Runtime.getRuntime().exec(runCmd);

            // 获取输入流
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            writer.write(input);
            writer.newLine();
            writer.flush();

            // 获取输出流
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            executeMessage.setMessage(sb.toString());

            // 等待命令执行结束并获取结果
            int exitCode = process.waitFor();
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
            executeMessage.setExitValue(exitCode);
            if (exitCode != 0) {
                //执行错误，读取程序执行后的报错信息
                errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorSb = new StringBuilder();
                String errorLine;
                while ((errorLine = errorBufferedReader.readLine()) != null) {
                    errorSb.append(errorLine);
                }
                executeMessage.setErrorMessage(errorSb.toString());
                executeMessage.setStatus(ExecuteCodeStatusEum.RUNNING_FAIL.getValue());
            }

        } catch (Exception e) {
            return ErrorUtils.getExecuteMessage(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    return ErrorUtils.getExecuteMessage(e);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
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
