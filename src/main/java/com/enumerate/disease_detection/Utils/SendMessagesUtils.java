package com.enumerate.disease_detection.Utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class SendMessagesUtils {

    /**
     * 发送统一格式的 WebSocket 消息 (协议 v2)
     *
     * @param session WebSocket 会话
     * @param type    消息类型 (thought, tool_call, tool_result, answer, error, etc.)
     * @param content 展示文本内容
     * @param payload 结构化数据 (可选)
     * @param tool    工具标识符 (仅对 tool_call, tool_result 有效)
     */
    public void sendEvent(WebSocketSession session, String type, String content, Object payload, String tool, String actionId) {
        if (session == null || !session.isOpen()) return;

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("type", type);
        messageMap.put("content", content);
        messageMap.put("timestamp", System.currentTimeMillis());

        if (payload != null) {
            messageMap.put("payload", payload);
        }

        if (tool != null && !tool.isEmpty()) {
            messageMap.put("tool", tool);
        }

        if (actionId != null && !actionId.isEmpty()) {
            messageMap.put("actionId", actionId);
        }

        com.enumerate.disease_detection.MVC.Controller.ChatWebSocketHandler.sendMessage(session, messageMap);
    }

    /**
     * 简化的发送方法 (无 payload, tool, actionId)
     */
    public void sendEvent(WebSocketSession session, String type, String content) {
        this.sendEvent(session, type, content, null, null, null);
    }

    /**
     * 发送包含内容和 payload 的方法
     */
    public void sendEvent(WebSocketSession session, String type, String content, Object payload) {
        this.sendEvent(session, type, content, payload, null, null);
    }

    /**
     * 发送包含内容、payload 和 tool 的方法
     */
    public void sendEvent(WebSocketSession session, String type, String content, Object payload, String tool) {
        this.sendEvent(session, type, content, payload, tool, null);
    }





    private void sendDataEvent(WebSocketSession session, int id, String type, String content) {
        if (session == null) return;
        com.enumerate.disease_detection.MVC.Controller.ChatWebSocketHandler.sendMessage(session, Map.of(
                "type", "data",
                "id", id,
                "dataType", type,
                "content", content,
                "timestamp", System.currentTimeMillis()
        ));
    }

    // Existing methods for SSE...
    private void sendStatusEvent(SseEmitter emitter, int id, String status, String message) {
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .id(String.valueOf(id))
                    .name("status")
                    .data(Map.of(
                            "status", status,
                            "message", message,
                            "timestamp", System.currentTimeMillis()
                    ));
            emitter.send(event);
        } catch (IOException e) {
            log.error("发送状态事件失败", e);
        }
    }


    private void sendDataEvent(SseEmitter emitter, int id, String type, String content) {
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .id(String.valueOf(id))
                    .name("data")
                    .data(Map.of(
                            "type", type,
                            "content", content,
                            "timestamp", System.currentTimeMillis()
                    ));
            emitter.send(event);
        } catch (IOException e) {
            log.error("发送数据事件失败", e);
        }
    }
}
