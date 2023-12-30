package com.zjh.ojcodesandbox;

import cn.hutool.core.io.resource.ResourceUtil;
import com.zjh.ojcodesandbox.impl.JavaDockerCodeArgsSandbox;
import com.zjh.ojcodesandbox.model.ExecuteCodeRequest;
import com.zjh.ojcodesandbox.model.ExecuteCodeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@SpringBootTest
class OjCodeSandboxApplicationTests {

    public static final String JAVA_CLASS_NAME = "Main.java";

    @Resource
    private JavaDockerCodeArgsSandbox javaDockerCodeSandbox;

    @Test
    void contextLoads() {
        String code = ResourceUtil.readStr(JAVA_CLASS_NAME, StandardCharsets.UTF_8);
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaDockerCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

}
