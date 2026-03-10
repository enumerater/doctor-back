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

    public void sendEvent(WebSocketSession session, String type, int id, String content, String payload, String tool) {
        if (session == null) return;

        // 1. 使用 HashMap 构建参数（而非不可变的 Map.of），支持动态添加非必填项
        Map<String, Object> messageMap = new HashMap<>();
        // 必选参数
        messageMap.put("type", type);
        messageMap.put("id", id);
        messageMap.put("content", content);
        messageMap.put("timestamp", System.currentTimeMillis());

        // 2. 非必填参数：仅当值不为 null/空字符串时才添加
        if (payload != null && !payload.isEmpty()) {
            messageMap.put("payload", payload);
        }
        if (tool != null && !tool.isEmpty()) {
            messageMap.put("tool", tool);
        }

        // 3. 发送消息
        com.enumerate.disease_detection.MVC.Controller.ChatWebSocketHandler.sendMessage(session, messageMap);
    }

    // 【推荐】重载方法：提供更易用的调用方式（无需传 null 给非必填参数）
    public void sendEvent(WebSocketSession session, String type, int id, String content) {
        // 调用原方法，非必填参数传 null
        this.sendEvent(session, type, id, content, null, null);
    }

    // 可选重载：只传 payload 不传 tool
    public void sendEvent(WebSocketSession session, String type, int id, String content, String payload) {
        this.sendEvent(session, type, id, content, payload, null);
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
