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
            @RequestParam Long sessionId
    ) throws IOException {
        log.info("Agent工作流开始处理");
        log.info("prompt: {} | image: {} {}", prompt, image,userId);

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

        String input = prompt;
        if (image != null && !image.isBlank() && !"null".equals(image)) {
            input = prompt + "\n\n[附图]: " + image;
        }
        agentWorkflowService.execute(emitter, input, userId);

        return emitter;
    }
}
