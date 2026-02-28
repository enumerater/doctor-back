package com.enumerate.disease_detection.MVC.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.MVC.Mapper.*;
import com.enumerate.disease_detection.MVC.POJO.PO.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务：异步处理用户对话记录，提取长期记忆
 * 每6小时执行一次
 */
@Service
@Slf4j
public class MemoryScheduledService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private MemoryProcessLogMapper memoryProcessLogMapper;

    @Autowired
    private MemoryExtractionService memoryExtractionService;

    /**
     * 每6小时执行一次，处理所有未提取记忆的会话
     */
    @Scheduled(cron = "0 0 0/6 * * ?")
//    @Scheduled(initialDelay = 5000, fixedDelay = Long.MAX_VALUE)
    public void processUserMemories() {
        log.info("===== 开始定时记忆提取任务 =====");

        // 1. 查询所有活跃用户
        List<UserPO> activeUsers = userMapper.selectList(
                new QueryWrapper<UserPO>()
                        .eq("deleted", "0")
                        .eq("status", "1")
        );

        log.info("活跃用户数: {}", activeUsers.size());

        int totalSessions = 0;
        int totalMemories = 0;

        for (UserPO user : activeUsers) {
            try {
                int[] result = processUserSessions(user);
                totalSessions += result[0];
                totalMemories += result[1];
            } catch (Exception e) {
                log.error("处理用户{}记忆失败", user.getId(), e);
            }
        }

        log.info("===== 记忆提取任务完成: 处理{}个会话, 提取{}条记忆 =====", totalSessions, totalMemories);
    }

    /**
     * 处理单个用户的所有未处理会话
     *
     * @return [处理会话数, 提取记忆数]
     */
    private int[] processUserSessions(UserPO user) {
        Long userId = user.getId();

        // 查询用户的所有未删除会话
        List<ChatSessionPO> sessions = sessionMapper.selectList(
                new QueryWrapper<ChatSessionPO>()
                        .eq("user_id", userId)
        );

        int processedCount = 0;
        int memoryCount = 0;

        for (ChatSessionPO session : sessions) {
            // 构造复合键（与 ChatService.memory() 一致: userId + sessionId）
            String compositeKey = String.valueOf(session.getSessionId());

            // 检查是否已处理
            boolean alreadyProcessed = memoryProcessLogMapper.exists(
                    new QueryWrapper<MemoryProcessLogPO>()
                            .eq("session_id", compositeKey)
            );

            if (alreadyProcessed) {
                continue;
            }

            // 查询该会话的所有消息
            List<ChatMessagePO> messages = chatMessageMapper.selectList(
                    new QueryWrapper<ChatMessagePO>()
                            .eq("session_id", compositeKey)
                            .eq("deleted", "0")
                            .orderByAsc("message_time")
            );

            if (messages.size() < 2) {
                // 消息不足，记录为跳过
                saveProcessLog(userId, compositeKey, 0, "SKIPPED", null);
                continue;
            }

            // 提取记忆
            try {
                int extracted = memoryExtractionService.extractAndSaveMemories(userId, compositeKey, messages);
                saveProcessLog(userId, compositeKey, extracted, "SUCCESS", null);
                processedCount++;
                memoryCount += extracted;
            } catch (Exception e) {
                log.error("会话{}记忆提取失败", compositeKey, e);
                saveProcessLog(userId, compositeKey, 0, "FAILED", e.getMessage());
            }
        }

        if (processedCount > 0) {
            log.info("用户{}处理了{}个会话，提取{}条记忆", userId, processedCount, memoryCount);
        }

        return new int[]{processedCount, memoryCount};
    }

    private void saveProcessLog(Long userId, String sessionId, int memoryCount, String status, String errorMessage) {
        MemoryProcessLogPO logPO = MemoryProcessLogPO.builder()
                .userId(userId)
                .sessionId(sessionId)
                .processedAt(LocalDateTime.now())
                .memoryCount(memoryCount)
                .status(status)
                .errorMessage(errorMessage)
                .build();
        memoryProcessLogMapper.insert(logPO);
    }
}
