package com.zjh.ojcodesandbox.exception;


import com.zjh.ojcodesandbox.model.ExecuteCodeResponse;
import com.zjh.ojcodesandbox.model.enums.ExecuteCodeStatusEum;
import com.zjh.ojcodesandbox.util.ErrorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ExecuteCodeResponse runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException: ", e);
        return ErrorUtils.getResponse(500, "系统错误");
    }

    @ExceptionHandler(BusinessException.class)
    public ExecuteCodeResponse businessExceptionHandler(BusinessException e) {
        log.error("BusinessException: ", e);
        return ErrorUtils.getResponse(e.getCode(), e.getMessage());
    }
}
