package com.zjh.ojcodesandbox.util;

import com.zjh.ojcodesandbox.model.ExecuteCodeResponse;
import com.zjh.ojcodesandbox.model.ExecuteMessage;
import com.zjh.ojcodesandbox.model.enums.ExecuteCodeStatusEum;

public class ErrorUtils {
    public static ExecuteCodeResponse getResponse(ExecuteCodeStatusEum executeCodeStatusEum) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setStatus(executeCodeStatusEum.getValue());
        executeCodeResponse.setMessage(executeCodeStatusEum.getText());
        return executeCodeResponse;
    }

    public static ExecuteCodeResponse get500Response(String message) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setStatus(500);
        executeCodeResponse.setMessage(message);
        return executeCodeResponse;
    }

    public static ExecuteMessage getExecuteMessage(Throwable e) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        executeMessage.setStatus(ExecuteCodeStatusEum.RUNNING_FAIL.getValue());
        executeMessage.setMessage(e.getMessage());
        return executeMessage;
    }
}
