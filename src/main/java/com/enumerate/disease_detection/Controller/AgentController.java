package com.enumerate.disease_detection.Controller;

import com.enumerate.disease_detection.Service.AgentWorkflowService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;

@RestController
@RequestMapping("/agent")
@Slf4j
public class AgentController {

    @Autowired
    private AgentWorkflowService agentWorkflowService;

    /**
     * Agent工作流端点 - 基于ReAct认知循环
     *
     * @param response HTTP响应
     * @param prompt 用户输入文本
     * @param image 图片URL
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param agentConfigId Agent配置ID（可选，不传则使用用户默认配置）
     * @return SSE流式响应
     */
    @GetMapping("/agriculture-agent")
    @CrossOrigin(
            origins = "http://localhost:5173",
            allowCredentials = "true",
            allowedHeaders = "*",
            methods = {GET, OPTIONS}
    )
    public SseEmitter agent(
            HttpServletResponse response,
            @RequestParam String prompt,
            @RequestParam String image,
            @RequestParam Long userId,
            @RequestParam Long sessionId,
            @RequestParam(required = false) Long agentConfigId
    ) throws IOException {
        log.info("Agent工作流开始处理");
        log.info("prompt: {} | image: {}", prompt, image);

        response.setContentType("text/event-stream;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");

        SseEmitter emitter = new SseEmitter(300 * 1000L);

        emitter.onCompletion(() -> log.info("SSE连接已完成"));
        emitter.onTimeout(() -> {
            log.info("SSE连接已超时");
            emitter.complete();
        });
        emitter.onError((e) -> {
            log.error("SSE连接发生错误", e);
            emitter.completeWithError(e);
        });

        agentWorkflowService.execute(emitter, prompt + " " + image, userId, agentConfigId);

        return emitter;
    }
}
