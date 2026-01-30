package com.enumerate.disease_detection.Controller;

import com.enumerate.disease_detection.Service.AgentService;
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
    private AgentService agentService;

    @GetMapping("/agriculture-agent")
    @CrossOrigin(
            origins = "http://localhost:5173", // 前端地址
            allowCredentials = "true", // Spring 5.x需用字符串"true"
            allowedHeaders = "*", // 允许所有请求头
            methods = {GET, OPTIONS} // 显式指定请求方法
    )
    public SseEmitter agent(HttpServletResponse response, @RequestParam String prompt, @RequestParam String image, @RequestParam Long userId, @RequestParam Long sessionId) throws IOException {
        log.info("request=============================================agent正在处理");
        log.info("prompt: " + prompt + " image: " + image);
        // 设置SSE响应头
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");
        
        // 创建SSE发射器，设置超时时间为5分钟
        SseEmitter emitter = new SseEmitter(300 * 1000L);
        
        // 处理连接关闭
        emitter.onCompletion(() -> {
            log.info("SSE连接已完成");
        });
        
        emitter.onTimeout(() -> {
            log.info("SSE连接已超时");
            emitter.complete();
        });
        
        emitter.onError((e) -> {
            log.error("SSE连接发生错误", e);
            emitter.completeWithError(e);
        });

        agentService.invokes(emitter, prompt + " " + image);

        return emitter;
    }
}