package com.enumerate.disease_detection.MVC.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class InteractionManager {

    // actionId -> CompletableFuture
    private final Map<String, CompletableFuture<Map<String, Object>>> pendingInteractions = new ConcurrentHashMap<>();

    public CompletableFuture<Map<String, Object>> createInteraction(String actionId) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        pendingInteractions.put(actionId, future);
        
        // Auto-remove after timeout (e.g., 5 minutes)
        future.orTimeout(5, TimeUnit.MINUTES).whenComplete((res, ex) -> {
            pendingInteractions.remove(actionId);
        });
        
        return future;
    }

    public void handleResponse(String actionId, Map<String, Object> response) {
        CompletableFuture<Map<String, Object>> future = pendingInteractions.get(actionId);
        if (future != null) {
            future.complete(response);
        } else {
            log.warn("Received response for unknown actionId: {}", actionId);
        }
    }
}
