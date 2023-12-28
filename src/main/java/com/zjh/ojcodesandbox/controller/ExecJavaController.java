package com.zjh.ojcodesandbox.controller;

import com.zjh.ojcodesandbox.impl.JavaDockerCodeSandbox;
import com.zjh.ojcodesandbox.impl.JavaNativeCodeSandbox;
import com.zjh.ojcodesandbox.model.ExecuteCodeRequest;
import com.zjh.ojcodesandbox.model.ExecuteCodeResponse;
import com.zjh.ojcodesandbox.model.enums.ExecuteCodeStatusEum;
import com.zjh.ojcodesandbox.util.ErrorUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/java")
public class ExecJavaController {
//    @Resource
//    private JavaNativeCodeSandbox javaNativeCodeSandbox;
    @Resource
    private JavaDockerCodeSandbox javaDockerCodeSandbox;

    //定义鉴权请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "auth";
    private static final String AUTH_REQUEST_SECRET = "secretKey";

    @PostMapping("/native/args")
    public ExecuteCodeResponse executeCode (@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request, HttpServletResponse response) {
        if (!AUTH_REQUEST_SECRET.equals(request.getHeader(AUTH_REQUEST_HEADER))) {
            response.setStatus(403);
            return ErrorUtils.getResponse(ExecuteCodeStatusEum.NO_AUTH);
        }
        if (executeCodeRequest == null) {
            return ErrorUtils.get500Response("请求参数为空");
        }
//        return javaNativeCodeSandbox.executeCodeArgs(executeCodeRequest);
        return javaDockerCodeSandbox.executeCodeArgs(executeCodeRequest);
    }
}
