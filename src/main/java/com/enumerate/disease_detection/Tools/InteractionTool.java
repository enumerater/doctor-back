package com.enumerate.disease_detection.Tools;

import com.enumerate.disease_detection.Annotations.ToolName;
import com.enumerate.disease_detection.Local.SessionHolder;
import com.enumerate.disease_detection.MVC.Service.InteractionManager;
import com.enumerate.disease_detection.Utils.SendMessagesUtils;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class InteractionTool {

    @Autowired
    private InteractionManager interactionManager;

    @Autowired
    private SendMessagesUtils sendMessagesUtils;

    @Tool("发起确认：当你想执行某项敏感操作（如：创建农场、更新数据、删除记录）时，必须先调用此工具向用户发起询问确认。返回'true'表示用户同意，'false'表示用户拒绝。")
    @ToolName("confirm_action")
    public String confirmAction(@P("给用户的确认提示信息，例如：'您确定要创建名为[我的农场]的农场吗？'") String message) {
        log.info("工具调用: 发起确认, message={}", message);
        WebSocketSession session = SessionHolder.getSession();
        if (session == null) return "error: No active session";

        String actionId = UUID.randomUUID().toString();
        CompletableFuture<Map<String, Object>> future = interactionManager.createInteraction(actionId);

        // 发送 confirm 消息到前端
        sendMessagesUtils.sendEvent(session, "confirm", message, null, null, actionId);

        try {
            // 等待用户响应 (阻塞当前 Agent 线程)
            Map<String, Object> result = future.get();
            Boolean confirmed = (Boolean) result.get("confirmed");
            log.info("用户确认结果: actionId={}, confirmed={}", actionId, confirmed);
            return confirmed != null ? confirmed.toString() : "false";
        } catch (Exception e) {
            log.error("等待用户确认超时或出错", e);
            return "false";
        }
    }

    @Tool("追问用户：当你发现执行某个操作缺少必要信息时（例如：用户想创建农场但没给面积），调用此工具向用户索取缺失的信息。")
    @ToolName("ask_user")
    public String askUser(@P("向用户提出的问题，例如：'请问您的农场面积是多少？'") String question) {
        log.info("工具调用: 追问用户, question={}", question);
        WebSocketSession session = SessionHolder.getSession();
        if (session == null) return "error: No active session";

        String actionId = UUID.randomUUID().toString();
        CompletableFuture<Map<String, Object>> future = interactionManager.createInteraction(actionId);

        // 发送 ask 消息到前端
        sendMessagesUtils.sendEvent(session, "ask", question, null, null, actionId);

        try {
            // 等待用户回答
            Map<String, Object> result = future.get();
            String answer = (String) result.get("content");
            log.info("用户回答结果: actionId={}, answer={}", actionId, answer);
            return answer != null ? answer : "用户未回答";
        } catch (Exception e) {
            log.error("等待用户回答超时或出错", e);
            return "用户未回答";
        }
    }
}
