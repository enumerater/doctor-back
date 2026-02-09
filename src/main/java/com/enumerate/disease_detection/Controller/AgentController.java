package com.enumerate.disease_detection.Controller;

import com.enumerate.disease_detection.Service.AgentService;
import com.enumerate.disease_detection.Service.ReActLoopService;
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

    @Autowired
    private ReActLoopService reActLoopService;

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

    /**
     * 企业级ReAct循环Agent端点 (v2.0)
     *
     * 核心改进：
     * 1. ReAct循环：计划 → 执行 → 观察 → 反思 → 决策
     * 2. 动态规划：根据任务复杂度生成执行计划
     * 3. 质量评估：置信度打分、自动重试
     * 4. 异常处理：优雅降级、备用方案
     * 5. 工作记忆：保存执行状态和中间结果
     *
     * @param response HTTP响应
     * @param prompt 用户输入文本
     * @param image 图片URL
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return SSE流式响应
     */
    @GetMapping("/agriculture-agent-v2")
    @CrossOrigin(
            origins = "http://localhost:5173",
            allowCredentials = "true",
            allowedHeaders = "*",
            methods = {GET, OPTIONS}
    )
    public SseEmitter agentV2(
            HttpServletResponse response,
            @RequestParam String prompt,
            @RequestParam String image,
            @RequestParam Long userId,
            @RequestParam Long sessionId
    ) throws IOException {
        log.info("========== 企业级ReAct循环Agent (v2.0) ==========");
        log.info("prompt: {} | image: {}", prompt, image);

        // 设置SSE响应头
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");

        // 创建SSE发射器，设置超时时间为5分钟
        SseEmitter emitter = new SseEmitter(300 * 1000L);

        // 处理连接关闭
        emitter.onCompletion(() -> log.info("SSE连接已完成"));
        emitter.onTimeout(() -> {
            log.info("SSE连接已超时");
            emitter.complete();
        });
        emitter.onError((e) -> {
            log.error("SSE连接发生错误", e);
            emitter.completeWithError(e);
        });

        // 执行ReAct循环
        reActLoopService.executeReActLoop(emitter, prompt + " " + image);

        return emitter;
    }
}