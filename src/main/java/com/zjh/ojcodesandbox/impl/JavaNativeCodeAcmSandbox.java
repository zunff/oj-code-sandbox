package com.zjh.ojcodesandbox.impl;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.io.resource.ResourceUtil;
import com.zjh.ojcodesandbox.model.ExecuteMessage;
import com.zjh.ojcodesandbox.template.JavaCodeSandboxTemplate;
import com.zjh.ojcodesandbox.util.ProcessUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class JavaNativeCodeAcmSandbox extends JavaCodeSandboxTemplate {

    @Override
    public List<ExecuteMessage> execCodeToMessageList(List<String> inputList, String userCodeParentPath) {
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String input : inputList) {
            //-cp相当于环境变量，当找不到那个类时会从这些路径上看看有没有，多个路径时win是用;划分，linux是用:划分
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s:%s -Djava.security.manager=MySecurityManager Main", userCodeParentPath + File.separator, SECURITY_MANAGER_PARENT_PATH);
            ExecuteMessage runExecuteMessage = ProcessUtils.runProcessInputScanner(runCmd, input);
            executeMessageList.add(runExecuteMessage);
        }
        return executeMessageList;
    }
}
