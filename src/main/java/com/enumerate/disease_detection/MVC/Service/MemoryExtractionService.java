package com.enumerate.disease_detection.MVC.Service;

import com.enumerate.disease_detection.MVC.POJO.PO.ChatMessagePO;
import com.enumerate.disease_detection.Utils.MysqlEmbeddingStore;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 记忆提取服务：从对话记录中通过LLM提取关键记忆点
 */
@Service
@Slf4j
public class MemoryExtractionService {

    @Resource(name = "tongYiModel")
    private OpenAiChatModel tongYiModel;

    @Autowired
    private MysqlEmbeddingStore mysqlEmbeddingStore;

    private static final String SYSTEM_PROMPT = """
            你是一个记忆提取助手。你的任务是从用户与AI助手的对话记录中，提取出关于用户的关键信息和记忆点。

            提取规则：
            1. 只提取关于用户本人的事实性信息（如种植作物、所在地区、农场规模、遇到的问题、偏好等）
            2. 每条记忆必须是一句独立的陈述句，能脱离上下文被理解
            3. 不要提取AI助手的回复内容或通用知识
            4. 不要提取打招呼、寒暄等无意义信息
            5. 如果对话中没有有价值的用户信息，直接回复"无"

            输出格式（严格遵守）：
            1. 第一条记忆
            2. 第二条记忆
            3. 第三条记忆

            示例输出：
            1. 用户在山东省种植番茄
            2. 用户的番茄最近出现了叶片发黄的问题
            3. 用户使用大棚种植方式
            """;

    private static final int MAX_CONVERSATION_LENGTH = 8000;

    /**
     * 从会话消息中提取记忆并存储
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     * @param messages  该会话的消息列表
     * @return 提取的记忆条数
     */
    public int extractAndSaveMemories(Long userId, String sessionId, List<ChatMessagePO> messages) {
        if (messages == null || messages.size() < 2) {
            log.info("会话{}消息不足2条，跳过提取", sessionId);
            return 0;
        }

        // 拼接对话文本
        StringBuilder conversationText = new StringBuilder();
        for (ChatMessagePO msg : messages) {
            String role = "0".equals(msg.getMessageRole()) ? "用户" : "助手";
            conversationText.append(role).append(": ").append(msg.getMessageContent()).append("\n");
            if (conversationText.length() > MAX_CONVERSATION_LENGTH) {
                break;
            }
        }

        // 截断防溢出
        String text = conversationText.length() > MAX_CONVERSATION_LENGTH
                ? conversationText.substring(0, MAX_CONVERSATION_LENGTH)
                : conversationText.toString();

        // 调用LLM提取记忆
        List<String> memories;
        try {
            memories = callLlmForExtraction(text);
        } catch (Exception e) {
            log.error("LLM提取记忆失败, 会话: {}", sessionId, e);
            return 0;
        }

        if (memories.isEmpty()) {
            log.info("会话{}未提取到有效记忆", sessionId);
            return 0;
        }

        // 逐条向量化并入库
        int savedCount = 0;
        for (String memory : memories) {
            try {
                mysqlEmbeddingStore.saveUserMemory(userId, memory, sessionId, "conversation_extract");
                savedCount++;
            } catch (Exception e) {
                log.error("保存记忆失败: {}", memory, e);
            }
        }

        log.info("会话{}提取并保存{}条记忆（共提取{}条）", sessionId, savedCount, memories.size());
        return savedCount;
    }

    /**
     * 调用LLM提取记忆点
     */
    private List<String> callLlmForExtraction(String conversationText) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(SYSTEM_PROMPT));
        messages.add(UserMessage.from("请从以下对话中提取用户的关键记忆点：\n\n" + conversationText));

        ChatRequest request = ChatRequest.builder()
                .messages(messages)
                .build();

        ChatResponse response = tongYiModel.chat(request);
        String output = response.aiMessage().text();

        if (output == null || output.isBlank() || output.trim().equals("无")) {
            return List.of();
        }

        return parseMemories(output);
    }

    /**
     * 解析LLM输出的编号列表为记忆条目
     */
    private List<String> parseMemories(String output) {
        List<String> memories = new ArrayList<>();
        String[] lines = output.split("\n");
        for (String line : lines) {
            // 去除编号前缀（如 "1. "、"2. " 等）
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            String content = trimmed.replaceFirst("^\\d+\\.\\s*", "");
            if (!content.isEmpty() && !content.equals("无")) {
                memories.add(content);
            }
        }
        return memories;
    }
}
