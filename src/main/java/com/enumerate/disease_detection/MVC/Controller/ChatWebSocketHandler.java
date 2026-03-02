package com.enumerate.disease_detection.MVC.Controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.enumerate.disease_detection.MVC.Service.AgentWorkflowService;
import com.enumerate.disease_detection.MVC.Service.InteractionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // Store sessions: userId -> session
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Autowired
    @Lazy
    private AgentWorkflowService agentWorkflowService;

    @Autowired
    private InteractionManager interactionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("WebSocket connected for userId: {}", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            userSessions.remove(userId);
            log.info("WebSocket disconnected for userId: {}", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long userId = (Long) session.getAttributes().get("userId");
        String payload = message.getPayload();
        log.info("【WebSocket】收到来自用户 {} 的原始消息: {}", userId, payload);

        if (userId == null) {
            log.error("【WebSocket】未获取到 userId");
            return;
        }

        try {
            JSONObject json = JSON.parseObject(payload);
            String type = json.getString("type");
            log.info("【WebSocket】消息类型: {}", type);

            if ("chat".equals(type)) {
                String content = json.getString("content");
                log.info("【WebSocket】正在调用 Agent 处理内容: {}", content);
                // Execute Agent Workflow
                agentWorkflowService.executeWs(session, content, userId);
            } else if ("interaction_response".equals(type)) {
                String actionId = json.getString("actionId");
                JSONObject data = json.getJSONObject("data");
                if (actionId != null) {
                    interactionManager.handleResponse(actionId, data);
                } else {
                    log.warn("【WebSocket】收到 interaction_response 但缺少 actionId");
                }
            }
        } catch (Exception e) {
            log.error("【WebSocket】处理消息出错", e);
        }
    }

    public static void sendMessage(WebSocketSession session, Object message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(JSON.toJSONString(message)));
            }
        } catch (IOException e) {
            log.error("Failed to send WebSocket message", e);
        }
    }

    public static WebSocketSession getSession(Long userId) {
        return userSessions.get(userId);
    }
}
