package com.enumerate.disease_detection.Utils.Tools;

import com.enumerate.disease_detection.ChatModel.MainModel;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VisioTool {

    @Autowired
    private MainModel mainModel;

    @Tool("主模型agent决策-引导")
    public String agent(String userMessage) {
        // 构建提示词，引导大模型总结主题
        String prompt = String.format(
                "test",
                userMessage
        );
        // 调用大模型生成主题
        return mainModel.tongYiModel().chat( prompt);
    }
}
