package com.zjh.ojcodesandbox.model;

import lombok.Data;

@Data
public class ExecuteMessage {

    /**
     * 程序退出值
     */
    private Integer exitValue;

    /**
     * 程序输出信息
     */
    private String message;

    /**
     * 运行状态
     */
    private Integer status;

    /**
     * 程序错误信息
     */
    private String errorMessage;

    /**
     * 消耗的时间
     */
    private Long time;

    /**
     * 消耗的内存
     */
    private Long memory;
}
