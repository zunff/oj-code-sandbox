package com.zjh.ojcodesandbox.exception;

import com.zjh.ojcodesandbox.model.enums.ExecuteCodeStatusEum;

/**
 * 自定义异常类
 */
public class BusinessException extends RuntimeException{
    /**
     * 状态码
     */
    private int code;

    public int getCode() {
        return code;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ExecuteCodeStatusEum eum, String message) {
        super(message);
        this.code = eum.getValue();
    }

    public BusinessException(ExecuteCodeStatusEum eum) {
        super(eum.getText());
        this.code = eum.getValue();
    }
}
