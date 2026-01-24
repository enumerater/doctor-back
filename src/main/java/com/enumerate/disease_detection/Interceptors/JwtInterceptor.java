package com.enumerate.disease_detection.Interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

@Configuration
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 核心：先放行OPTIONS预检请求（无Token也允许通过）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 原有JWT Token校验逻辑（仅对非OPTIONS请求生效）
        String token = request.getHeader("Authorization");
        // 示例：你的Token校验逻辑（根据实际业务调整）
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token不能为空");
        }
        // 校验Token有效性...（比如解析Token、验证签名等）

        // 校验通过则放行
        return true;
    }
}