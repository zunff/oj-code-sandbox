package com.zjh.ojcodesandbox.impl;

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
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main", userCodeParentPath + File.separator);
            ExecuteMessage runExecuteMessage = ProcessUtils.runProcessInputScanner(runCmd, input);
            executeMessageList.add(runExecuteMessage);
        }
        return executeMessageList;
    }
}
