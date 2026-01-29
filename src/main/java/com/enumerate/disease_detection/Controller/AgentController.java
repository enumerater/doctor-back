package com.enumerate.disease_detection.Controller;

import com.enumerate.disease_detection.Service.AgentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;

@RestController
@RequestMapping("/agent")
@Slf4j
public class AgentController {

    @Autowired
    private AgentService agentService;


//    @GetMapping
//    @CrossOrigin(
//            origins = "http://localhost:5173", // 前端地址
//            allowCredentials = "true", // Spring 5.x需用字符串"true"
//            allowedHeaders = "*", // 允许所有请求头
//            methods = {GET, OPTIONS} // 显式指定请求方法
//    )
//    public SseEmitter agent(HttpServletResponse response, @RequestParam String prompt, @RequestParam Long userId, @RequestParam Long sessionId) {
//        // ========== 1. 配置【流式必备的3个核心响应头】，缺一不可 ==========
//        response.setContentType("text/event-stream;charset=UTF-8"); // 声明SSE流式格式
//        response.setHeader("Cache-Control", "no-cache"); // 禁止客户端缓存，防止分片错乱
//        response.setHeader("X-Accel-Buffering", "no"); // 禁止nginx反向代理缓冲，部署必加
//
//        // ========== 2. 创建流式发射器，设置超时时间60秒（按需调整） =========
//        SseEmitter emitter = new SseEmitter(60 * 1000L);
//        agentService.agent(emitter, prompt, userId, sessionId);
//        return emitter;
//    }

    @GetMapping
    public SseEmitter agent(HttpServletResponse response) throws IOException {
        log.info("request=============================================agent正在处理");
        
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

        agentService.invokes(emitter, "{\"content\":\"这是我的葡萄，叶子上有病害，怎么办\"，\"image\":\"\"}");

        return emitter;
    }
}