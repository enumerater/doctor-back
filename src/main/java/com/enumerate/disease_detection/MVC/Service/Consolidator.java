package com.enumerate.disease_detection.MVC.Service;

import com.enumerate.disease_detection.ChatModel.PersistentChatMemoryStore;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class Consolidator {

    private static final int CONTEXT_WINDOW_TOKENS = 512;
    private static final int MAX_COMPLETION_TOKENS = 128;
    private static final int SAFETY_BUFFER = 64;
    private final int TOKEN_BUDGET = CONTEXT_WINDOW_TOKENS - MAX_COMPLETION_TOKENS - SAFETY_BUFFER;

    @Autowired
    private PersistentChatMemoryStore shortTermStore;
    @Autowired
    private ProjectMemoryStore longTermStore;

    @Resource(name = "tongYiModel")
    private OpenAiChatModel model;

    // ====================== 核心：增量压缩（对齐Python） ======================
    public void maybeConsolidate(Object memoryId, Long userId, List<ChatMessage> allMessages, Integer lastConsolidated) {
        // 1. 基础校验
        if (allMessages.isEmpty()) return;
        int totalSize = allMessages.size();

        // 2. 计算Token
        int currentTokens = calculateTokens(allMessages);
        log.info("会话Token消耗：{}/{}", currentTokens, TOKEN_BUDGET);
        if (currentTokens <= TOKEN_BUDGET) return;

        // 3. 【关键】只压缩 上次压缩游标之后 的消息（不重复压缩）
        int startIndex = lastConsolidated;
        // 至少留5条新消息不压缩，直接跳过
        if (startIndex >= totalSize - 5) {
            log.info("无新的可压缩消息，跳过");
            return;
        }

        // 4. 安全截取：修复【负数索引】核心问题
        // 保留消息数量
        int keepCount = Math.max(10, totalSize / 2);
        // ✅ 修复1：强制保证移除数量 ≥ 0，避免负数
        int removeCount = Math.max(0, totalSize - keepCount);
        int endIndex = startIndex + removeCount;
        if (endIndex > totalSize) endIndex = totalSize;

        // ✅ 修复2：防护核心！如果结束索引 ≤ 起始索引，直接跳过（杜绝subList报错）
        if (endIndex <= startIndex) {
            log.info("无有效可压缩消息，跳过");
            return;
        }

        List<ChatMessage> oldMessages = allMessages.subList(startIndex, endIndex);
        if (oldMessages.isEmpty()) return;

        // 5. 压缩+入库（只执行1次）
        String summary = generateSummary(oldMessages);
        longTermStore.appendCompressedHistory(userId, memoryId.toString(), summary);

        // 6. 保留新消息 + 更新游标（下次不再压缩这批）
        List<ChatMessage> keepMessages = allMessages.subList(endIndex, totalSize);
        shortTermStore.updateMessages(memoryId, keepMessages);

        log.info("✅ 增量压缩完成：压缩{}条新消息，游标更新至{}", oldMessages.size(), endIndex);
    }

    // ====================== 工具方法（无修改，保持原样） ======================
    private int calculateTokens(List<ChatMessage> messages) {
        int total = 0;
        for (ChatMessage msg : messages) {
            String content = getMessageContent(msg);
            if (content != null) total += estimateChineseToken(content);
        }
        return total;
    }

    private int estimateChineseToken(String text) {
        if (text == null || text.isBlank()) return 0;
        double tokenCount = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            tokenCount += isChineseChar(c) ? 1 : 0.5;
        }
        return (int) Math.ceil(tokenCount);
    }

    private boolean isChineseChar(char c) {
        return Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || (c >= '\u3000' && c <= '\u303F');
    }

    private String getMessageContent(ChatMessage message) {
        if (message instanceof UserMessage u) return u.singleText();
        if (message instanceof AiMessage a) return a.text();
        return null;
    }

    private String generateSummary(List<ChatMessage> messages) {
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("提取对话关键事实：用户信息、决策、解决方案、偏好、事件。简洁要点\n");
            for (ChatMessage msg : messages) {
                String c = getMessageContent(msg);
                if (c != null) prompt.append(msg instanceof UserMessage ? "用户：" : "AI：").append(c).append("\n");
            }
            return model.chat(String.valueOf(prompt));
        } catch (Exception e) {
            log.error("摘要生成失败", e);
            return "压缩失败";
        }
    }
}