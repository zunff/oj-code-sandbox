package com.zjh.ojcodesandbox.template;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.zjh.ojcodesandbox.CodeSandbox;
import com.zjh.ojcodesandbox.model.ExecuteCodeRequest;
import com.zjh.ojcodesandbox.model.ExecuteCodeResponse;
import com.zjh.ojcodesandbox.model.ExecuteMessage;
import com.zjh.ojcodesandbox.model.JudgeInfo;
import com.zjh.ojcodesandbox.model.enums.ExecuteCodeStatusEum;
import com.zjh.ojcodesandbox.util.ErrorUtils;
import com.zjh.ojcodesandbox.util.ProcessUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public abstract class JavaCodeSandboxTemplate implements CodeSandbox {

    protected static final String CODE_DIR_NAME = "tempCode";
    protected static final String JAVA_CLASS_NAME = "Main.java";

    protected static String SECURITY_MANAGER_PARENT_PATH;

    protected static final WordTree WORD_TREE;

    static {
        List<String> blackList = Arrays.asList("File", "exec", "sleep");
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(blackList);
    }

    /**
     * 1.将代码存储到本地
     *
     * @param code
     * @return
     */
    public File saveCodeToFile(String code) {
        FoundWord foundWord = WORD_TREE.matchWord(code);
        if (foundWord != null) {
            throw new RuntimeException("包含禁止词：" + foundWord.getFoundWord());
        }
        String codePathName = System.getProperty("user.dir") + File.separator + CODE_DIR_NAME;
        if (!FileUtil.exist(codePathName)) {
            FileUtil.mkdir(codePathName);
        }
        //存储code
        String userCodePathParentName = codePathName + File.separator + UUID.randomUUID();
        String userCodePathName = userCodePathParentName + File.separator + JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePathName, StandardCharsets.UTF_8);
        return userCodeFile;
    }

    /**
     * 2.对code进行编译
     *
     * @param userCodeFile
     * @return
     */
    public ExecuteMessage compileCode(File userCodeFile) {
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        return ProcessUtils.runProcessInputArgs(compileCmd, 0);
    }

    /**
     * 3.对编译出来的字节码文件进行执行
     *
     * @param inputList
     * @param userCodeParentPath
     * @return
     */
    public List<ExecuteMessage> execCodeToMessageList(List<String> inputList, String userCodeParentPath) {
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String input : inputList) {
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s:%s -Djava.security.manager=MySecurityManager Main %s", userCodeParentPath + File.separator, SECURITY_MANAGER_PARENT_PATH, input);
            ExecuteMessage runExecuteMessage = ProcessUtils.runProcessInputArgs(runCmd, 1);
            executeMessageList.add(runExecuteMessage);
        }
        return executeMessageList;
    }


    /**
     * 4.处理输出结果
     *
     * @param executeMessageList
     * @return
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        //先标记为成功，如果判断到有案例执行报错，再改为失败
        executeCodeResponse.setStatus(ExecuteCodeStatusEum.SUCCESS.getValue());
        //运行时间取所有用例中的最大值
        Long maxTime = 0L;
        for (ExecuteMessage executeMessage : executeMessageList) {
            if (!executeMessage.getStatus().equals(ExecuteCodeStatusEum.SUCCESS.getValue())) {
                //执行失败，记录错误，便于返回给用户
                executeCodeResponse.setStatus(executeMessage.getStatus());
                executeCodeResponse.setMessage(executeMessage.getErrorMessage());
                //执行错误存个error串展位
                outputList.add("error");
                //跳过下面的逻辑
                continue;
            }
            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
            outputList.add(executeMessage.getMessage());
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        //获取占用内存非常麻烦，暂时跳过
//        judgeInfo.setMemory(0L);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    /**
     * 5.文件清理
     *
     * @param userCodeFile
     */
    public void delTemp(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            FileUtil.del(userCodeFile.getParentFile().getAbsolutePath());
        }
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        ClassPathResource classPathResource = new ClassPathResource("/security");
        SECURITY_MANAGER_PARENT_PATH = classPathResource.getFile().getAbsolutePath();

        List<String> inputList = executeCodeRequest.getInputList();
        //1.将代码存储到本地
        File userCodeFile = null;
        try {
            userCodeFile = saveCodeToFile(executeCodeRequest.getCode());
        } catch (Exception e) {
            return ErrorUtils.getResponse(ExecuteCodeStatusEum.COMPILE_FAIL, e.getMessage());
        }

        //2.对code进行编译
        ExecuteMessage compileExecuteMessage = compileCode(userCodeFile);
        //如果编译失败，记录错误信息并返回
        if (compileExecuteMessage.getExitValue() != 0) {
            ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
            executeCodeResponse.setStatus(ExecuteCodeStatusEum.COMPILE_FAIL.getValue());
            executeCodeResponse.setMessage(compileExecuteMessage.getErrorMessage());
            return executeCodeResponse;
        }

        //3.对编译出来的字节码文件进行执行
        List<ExecuteMessage> executeMessageList = execCodeToMessageList(inputList, userCodeFile.getParentFile().getAbsolutePath());

        //4.处理输出结果
        ExecuteCodeResponse executeCodeResponse = getOutputResponse(executeMessageList);

        //5.文件清理
        delTemp(userCodeFile);

        return executeCodeResponse;
    }

}
