package com.enumerate.disease_detection.MVC.Controller;



import com.enumerate.disease_detection.MVC.Service.ChatService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/stream")
    public SseEmitter stream(HttpServletResponse response, @RequestParam String question) {
        // ========== 1. 配置【流式必备的3个核心响应头】，缺一不可 ==========
        response.setContentType("text/event-stream;charset=UTF-8"); // 声明SSE流式格式
        response.setHeader("Cache-Control", "no-cache"); // 禁止客户端缓存，防止分片错乱
        response.setHeader("X-Accel-Buffering", "no"); // 禁止nginx反向代理缓冲，部署必加

        // ========== 2. 创建流式发射器，设置超时时间60秒（按需调整） ==========
        SseEmitter emitter = new SseEmitter(60 * 1000L);

        // ========== 3. 调用业务层方法，传入发射器和提问话术，业务逻辑交给Service ==========
        chatService.stream(emitter, question);

        // ========== 4. 返回发射器，Spring自动维护TCP长连接 ==========
        return emitter;
    }

    @GetMapping("/deepThink")
    public SseEmitter deepThink(HttpServletResponse response, @RequestParam String question) {
        // ========== 1. 配置【流式必备的3个核心响应头】，缺一不可 ==========
        response.setContentType("text/event-stream;charset=UTF-8"); // 声明SSE流式格式
        response.setHeader("Cache-Control", "no-cache"); // 禁止客户端缓存，防止分片错乱
        response.setHeader("X-Accel-Buffering", "no"); // 禁止nginx反向代理缓冲，部署必加

        // ========== 2. 创建流式发射器，设置超时时间60秒（按需调整） ==========
        SseEmitter emitter = new SseEmitter(60 * 1000L);

        // ========== 3. 调用业务层方法，传入发射器和提问话术，业务逻辑交给Service ==========
        chatService.deepThink(emitter, question);

        // ========== 4. 返回发射器，Spring自动维护TCP长连接 ==========
        return emitter;
    }

    @GetMapping("/memory")
    @CrossOrigin(
            origins = "http://localhost:5173", // 前端地址
            allowCredentials = "true", // Spring 5.x需用字符串"true"
            allowedHeaders = "*", // 允许所有请求头
            methods = {GET, OPTIONS} // 显式指定请求方法
    )
    public SseEmitter memory(HttpServletResponse response, @RequestParam String prompt, @RequestParam String image, @RequestParam Long userId, @RequestParam Long sessionId, @RequestParam String model) {
        // ========== 1. 配置【流式必备的3个核心响应头】，缺一不可 ==========
        response.setContentType("text/event-stream;charset=UTF-8"); // 声明SSE流式格式
        response.setHeader("Cache-Control", "no-cache"); // 禁止客户端缓存，防止分片错乱
        response.setHeader("X-Accel-Buffering", "no"); // 禁止nginx反向代理缓冲，部署必加

        // ========== 2. 创建流式发射器，设置超时时间60秒（按需调整） =========
        SseEmitter emitter = new SseEmitter(60 * 1000L);
        chatService.memory(emitter, prompt, image, userId, sessionId,model);
        return emitter;
    }


}
