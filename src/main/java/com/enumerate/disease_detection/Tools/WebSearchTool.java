package com.enumerate.disease_detection.Tools;

import com.enumerate.disease_detection.Annotations.ToolName;
import com.enumerate.disease_detection.Utils.ToolApiClient;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class WebSearchTool {

    @Tool("联网搜索工具，可以搜索互联网上的最新信息。")
    @ToolName("联网搜索")
    public String webSearch(@P("搜索关键词") String query) {
        log.info("工具调用: 联网搜索, query={}", query);

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("query", query);

            // 流式调用：用StringBuilder收集结果（也可直接实时返回给前端）
            StringBuilder resultBuilder = new StringBuilder();
            // 异步调用（避免阻塞主线程）
            CompletableFuture.runAsync(() -> {
                try {
                    ToolApiClient.callStreamApi("/api/web-search", params, 30, chunk -> {
                        // 实时处理每一块流式数据
                        log.info("收到流式数据: {}", chunk);
                        resultBuilder.append(chunk); // 收集结果（按需替换为前端推送逻辑）
                    });
                } catch (Exception e) {
                    log.error("流式搜索失败", e);
                    resultBuilder.append("搜索失败: ").append(e.getMessage());
                }
            }).get(); // 等待流式调用完成（如果是Web场景，应直接返回SSE给前端，无需get()）

            String result = resultBuilder.toString();
            log.info("联网搜索最终结果: {}", result);
            return result;
        } catch (Exception e) {
            log.error("联网搜索失败", e);
            return "联网搜索失败: " + e.getMessage();
        }
    }

}