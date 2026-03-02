package com.enumerate.disease_detection.Interceptors;

import com.enumerate.disease_detection.Properties.JwtProperties;
import com.enumerate.disease_detection.Utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String token = servletRequest.getServletRequest().getParameter("token");

            if (token != null && !token.isEmpty()) {
                try {
                    Claims claims = JwtUtil.parseJWT(jwtProperties.getSecret(), token);
                    Long userId = Long.valueOf(claims.get("id").toString());
                    attributes.put("userId", userId);
                    log.info("WebSocket handshake successful for userId: {}", userId);
                    return true;
                } catch (Exception e) {
                    log.error("WebSocket JWT validation failed: {}", e.getMessage());
                }
            }
        }
        log.error("WebSocket handshake failed: Missing or invalid token");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
