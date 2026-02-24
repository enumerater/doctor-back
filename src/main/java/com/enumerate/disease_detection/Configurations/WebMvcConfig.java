package com.enumerate.disease_detection.Configurations;

import com.enumerate.disease_detection.Interceptors.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 必须加@Configuration，确保被Spring扫描
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final JwtInterceptor jwtInterceptor;

    // 构造注入JWT拦截器
    public WebMvcConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**") // 拦截所有路径
                // 仅排除登录、注册接口（无需处理OPTIONS，拦截器内已放行）
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/user/sendCode",
                        "/api/user/emailLogin",
                        // 兼容本地调试的路径（保留原路径，不影响本地开发）
                        "/user/login",
                        "/user/register",
                        "/user/sendCode",
                        "/user/emailLogin"
                );
    }
}