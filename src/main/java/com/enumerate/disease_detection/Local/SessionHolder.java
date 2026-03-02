package com.enumerate.disease_detection.Local;

import org.springframework.web.socket.WebSocketSession;

public class SessionHolder {
    private static final ThreadLocal<WebSocketSession> sessionThreadLocal = new ThreadLocal<>();

    public static void setSession(WebSocketSession session) {
        sessionThreadLocal.set(session);
    }

    public static WebSocketSession getSession() {
        return sessionThreadLocal.get();
    }

    public static void removeSession() {
        sessionThreadLocal.remove();
    }
}
