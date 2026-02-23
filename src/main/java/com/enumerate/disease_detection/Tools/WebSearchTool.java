package com.enumerate.disease_detection.Tools;

import com.enumerate.disease_detection.Utils.ToolApiClient;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class WebSearchTool {

    @Tool("联网搜索工具，可以搜索互联网上的最新信息。当用户询问最新政策、实时新闻、最新防治技术、市场价格等需要实时网络信息的问题时应调用此工具。例如：'最新的草地贪夜蛾防治政策'、'今年小麦价格'等。")
    public String webSearch(@P("搜索关键词") String query) {
        log.info("工具调用: 联网搜索, query={}", query);

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("query", query);

            String response = ToolApiClient.callPostApi("/api/web-search", params, 30);
            String result = ToolApiClient.extractResult(response);

            log.info("联网搜索结果: {}", result);
            return result;
        } catch (Exception e) {
            log.error("联网搜索失败", e);
            return "联网搜索失败: " + e.getMessage();
        }
    }
}
