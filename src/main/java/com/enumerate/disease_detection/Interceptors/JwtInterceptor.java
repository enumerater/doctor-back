package com.enumerate.disease_detection.Interceptors;

import com.enumerate.disease_detection.Local.UserContextHolder;
import com.enumerate.disease_detection.Properties.JwtProperties;
import com.enumerate.disease_detection.Utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

@Configuration
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;



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

        log.info("Token: {}", token);

        try {
            // 校验Token有效性
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecret(), token);
            if (claims == null) {
                throw new RuntimeException("Token无效");
            }

            Long id = (Long) claims.get("id");
            UserContextHolder.setUserId(id);

        } catch (Exception e) {
            log.error("JWT校验失败: {}", e.getMessage());
            throw new RuntimeException("Token无效");
        }

        // 校验通过则放行
        return true;
    }
}