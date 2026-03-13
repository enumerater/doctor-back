package com.enumerate.disease_detection.MVC.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.enumerate.disease_detection.MVC.Mapper.ChatMessageMapper;
import com.enumerate.disease_detection.MVC.Mapper.SessionMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.ChatMessagePO;
import com.enumerate.disease_detection.MVC.POJO.PO.ChatSessionPO;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话汇总服务，用于异步生成会话标题
 */
@Service
@Slf4j
public class SessionSummaryService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private SessionMapper sessionMapper;

    @Resource(name = "tongYiModel")
    private OpenAiChatModel chatLanguageModel;

    /**
     * 异步生成并更新会话标题
     *
     * @param sessionId 会话的业务ID (String类型)
     */
    @Async("aiAsyncExecutor")
    public void generateSummary(String sessionId) {
        try {
            // 延迟30秒，给用户留出发送第一条或前几条消息的时间
            log.info("会话汇总任务已启动，等待30秒后开始处理会话: {}", sessionId);
            Thread.sleep(30000);
            
            log.info("开始为会话 {} 获取消息内容并生成标题", sessionId);

            // 获取前5条消息作为摘要上下文
            List<ChatMessagePO> messages = chatMessageMapper.selectList(new QueryWrapper<ChatMessagePO>()
                    .eq("session_id", sessionId)
                    .orderByAsc("message_time")
                    .last("LIMIT 5"));

            if (messages.isEmpty()) {
                log.info("会话 {} 目前没有消息，取消生成标题汇总", sessionId);
                return;
            }

            // 构建对话上下文
            String conversationContext = messages.stream()
                    .map(m -> ("0".equals(m.getMessageRole()) ? "用户: " : "助手: ") + m.getMessageContent())
                    .collect(Collectors.joining("\n"));

            // 构造Prompt
            String prompt = "你是一个会话标题生成助手。请根据以下对话内容，生成一个简短、概括性的标题（不超过15个字）。" +
                    "标题应该直接反映对话的核心主题。只返回标题内容，不要包含引号、前缀或其他解释性文字。\n\n对话内容：\n" + 
                    conversationContext;

            log.info("正在调用AI模型生成标题...");
            String summaryTitle = chatLanguageModel.chat(prompt);
            
            if (summaryTitle != null && !summaryTitle.trim().isEmpty()) {
                // 清理生成的标题
                summaryTitle = summaryTitle.trim().replaceAll("^\"|\"$", "").replaceAll("^标题[:：]", "");
                
                // 更新会话标题
                UpdateWrapper<ChatSessionPO> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("session_id", sessionId)
                        .set("session_title", summaryTitle);
                
                int rows = sessionMapper.update(null, updateWrapper);
                if (rows > 0) {
                    log.info("会话 {} 标题已成功更新为: {}", sessionId, summaryTitle);
                } else {
                    log.warn("未能找到会话 {} 进行标题更新", sessionId);
                }
            } else {
                log.warn("AI模型未返回有效的标题内容");
            }

        } catch (InterruptedException e) {
            log.error("会话标题生成任务被意外中断", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("会话标题生成过程中发生异常", e);
        }
    }
}
