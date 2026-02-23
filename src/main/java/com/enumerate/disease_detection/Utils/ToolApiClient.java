package com.enumerate.disease_detection.Utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * Tool API统一调用客户端
 * 用于调用Python FastAPI的各个Tool接口
 */
@Slf4j
public class ToolApiClient {

    // 全局HttpClient实例（单例复用）
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    // Python FastAPI服务基础地址
    private static final String FAST_API_BASE_URL = "http://127.0.0.1:8000";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调用GET类型的Tool API
     *
     * @param endpoint API端点路径
     * @param params   参数Map
     * @param timeout  超时时间（秒）
     * @return API响应结果
     * @throws Exception 调用异常
     */
    public static String callGetApi(String endpoint, Map<String, Object> params, int timeout) throws Exception {
        // 构建请求URL
        StringBuilder urlBuilder = new StringBuilder(FAST_API_BASE_URL).append(endpoint);

        if (params != null && !params.isEmpty()) {
            urlBuilder.append("?");
            boolean first = true;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (!first) {
                    urlBuilder.append("&");
                }
                String encodedValue = URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8);
                urlBuilder.append(entry.getKey()).append("=").append(encodedValue);
                first = false;
            }
        }

        String requestUrl = urlBuilder.toString();
        log.info("调用Tool API [GET]: {}", requestUrl);

        // 构建请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .GET()
                .timeout(Duration.ofSeconds(timeout))
                .header("Accept", "application/json")
                .build();

        // 发送请求
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API调用失败，状态码: " + response.statusCode() + ", 响应: " + response.body());
        }

        log.info("Tool API响应: {}", response.body());
        return response.body();
    }

    /**
     * 调用POST类型的Tool API
     *
     * @param endpoint API端点路径
     * @param params   参数Map
     * @param timeout  超时时间（秒）
     * @return API响应结果
     * @throws Exception 调用异常
     */
    public static String callPostApi(String endpoint, Map<String, Object> params, int timeout) throws Exception {
        String requestUrl = FAST_API_BASE_URL + endpoint;
        log.info("调用Tool API [POST]: {}", requestUrl);

        // 将参数转为JSON
        String jsonBody = objectMapper.writeValueAsString(params);
        log.info("请求Body: {}", jsonBody);

        // 构建请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(timeout))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        // 发送请求
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API调用失败，状态码: " + response.statusCode() + ", 响应: " + response.body());
        }

        log.info("Tool API响应: {}", response.body());
        return response.body();
    }

    /**
     * 调用SSE流式API（用于需要流式返回的Tool）
     *
     * @param endpoint API端点路径
     * @param params   参数Map
     * @param timeout  超时时间（秒）
     * @return 完整的流式响应内容
     * @throws Exception 调用异常
     */
    public static String callStreamApi(String endpoint, Map<String, Object> params, int timeout) throws Exception {
        // 构建请求URL
        StringBuilder urlBuilder = new StringBuilder(FAST_API_BASE_URL).append(endpoint);

        if (params != null && !params.isEmpty()) {
            urlBuilder.append("?");
            boolean first = true;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (!first) {
                    urlBuilder.append("&");
                }
                String encodedValue = URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8);
                urlBuilder.append(entry.getKey()).append("=").append(encodedValue);
                first = false;
            }
        }

        String requestUrl = urlBuilder.toString();
        log.info("调用Tool Stream API: {}", requestUrl);

        // 构建请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .GET()
                .timeout(Duration.ofSeconds(timeout))
                .header("Accept", "text/event-stream")
                .header("Cache-Control", "no-cache")
                .build();

        // 流式处理响应
        StringBuilder result = new StringBuilder();
        HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofLines()).body()
                .forEach(line -> {
                    if (line != null && !line.isBlank()) {
                        result.append(line).append("\n");
                    }
                });

        log.info("Stream API响应: {}", result.toString());
        return result.toString().trim();
    }

    /**
     * 解析JSON响应，提取result字段
     *
     * @param jsonResponse JSON响应字符串
     * @return 提取的result内容
     */
    public static String extractResult(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            if (rootNode.has("result")) {
                return rootNode.get("result").asText();
            } else if (rootNode.has("data")) {
                return rootNode.get("data").toString();
            }
            return jsonResponse;
        } catch (Exception e) {
            log.warn("解析JSON响应失败，返回原始内容: {}", e.getMessage());
            return jsonResponse;
        }
    }
}
