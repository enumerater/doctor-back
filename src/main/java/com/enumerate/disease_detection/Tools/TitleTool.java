package com.enumerate.disease_detection.Tools;

import com.enumerate.disease_detection.ChatModel.MainModel;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class TitleTool {

    @Autowired
    private MainModel mainModel;

    @Tool("总结用户会话的主题，要求简洁、精准，不超过20个字")
    public String summarizeConversationTopic(String userFirstMessage) {
        // 构建提示词，引导大模型总结主题
        String prompt = String.format(
                "请总结以下用户对话的核心主题，要求简洁、精准，不超过20个字：\n用户消息：%s",
                userFirstMessage
        );
        // 调用大模型生成主题
        return mainModel.tongYiModel().chat( prompt);
    }
}
