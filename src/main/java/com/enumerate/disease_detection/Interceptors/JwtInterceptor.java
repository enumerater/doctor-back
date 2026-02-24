package com.enumerate.disease_detection.Interceptors;

import com.enumerate.disease_detection.Local.UserContextHolder;
import com.enumerate.disease_detection.Properties.JwtProperties;
import com.enumerate.disease_detection.Utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component; // 注意：拦截器用@Component，不是@Configuration
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component // 改为@Component，让Spring能扫描并注入
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 先放行OPTIONS预检请求（跨域必备）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2. 获取请求路径，处理/api前缀（兼容Nginx代理）
        String requestURI = request.getRequestURI();
        String path = requestURI.startsWith("/api/") ? requestURI.substring(4) : requestURI; // 去掉/api/前缀

        // 3. 定义需要放行的接口（无需Token的接口）
        String[] excludePaths = {
                "/user/login",
                "/user/register",
                "/user/sendCode",
                "/user/emailLogin"
        };
        // 遍历判断是否是放行路径，是则直接放行，不校验Token
        for (String excludePath : excludePaths) {
            if (excludePath.equals(path)) {
                log.info("放行无需Token的请求：{}", requestURI);
                return true;
            }
        }

        // 4. 非放行路径，执行原有Token校验逻辑
        String token = request.getHeader("Authorization");
        // 不再抛RuntimeException，而是返回401（未授权），避免500错误
        if (token == null || token.isEmpty()) {
            log.error("Token为空，请求路径：{}", requestURI);
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401状态码
            response.getWriter().write("{\"code\":401,\"msg\":\"Token不能为空\"}");
            return false; // 返回false，终止请求
        }

        log.info("Token: {}", token);

        try {
            // 校验Token有效性
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecret(), token);
            if (claims == null) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"code\":401,\"msg\":\"Token无效\"}");
                return false;
            }

            Long id = (Long) claims.get("id");
            UserContextHolder.setUserId(id);

        } catch (Exception e) {
            log.error("JWT校验失败: {}", e.getMessage());
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"msg\":\"Token无效\"}");
            return false;
        }

        // 5. 校验通过则放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContextHolder.removeUserId();
        UserContextHolder.removeUserIp();
    }
}