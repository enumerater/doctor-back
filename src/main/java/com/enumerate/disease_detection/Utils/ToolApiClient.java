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
import java.util.function.Consumer;

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
     * 真正的流式调用方法（POST）- 修复受限请求头问题
     * @param endpoint API端点
     * @param params   POST参数（JSON Body）
     * @param timeout  超时时间（秒）
     * @param callback 流式数据回调（每收到一块数据就触发）
     * @throws Exception 调用异常
     */
    public static void callStreamApi(String endpoint, Map<String, Object> params, int timeout, Consumer<String> callback) throws Exception {
        String requestUrl = FAST_API_BASE_URL + endpoint;
        log.info("调用流式API [POST]: {}", requestUrl);

        // 构建JSON请求体
        String jsonBody = objectMapper.writeValueAsString(params);
        log.info("流式请求Body: {}", jsonBody);

        // 构建请求（移除受限的Connection头）
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(timeout))
                // 保留必要头，移除Connection（受限头）
                .header("Accept", "text/event-stream")
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Cache-Control", "no-cache")
                .header("Accept-Charset", "UTF-8")
                // 移除 ↓ 这行是导致异常的根源
                // .header("Connection", "keep-alive")
                .build();

        // 核心：实时消费流式响应（优化为按行读取，避免单字节乱码）
        try (var inputStream = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream()).body()) {
            // 按行读取流式数据（比单字节更高效，避免中文乱码）
            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        callback.accept(line); // 实时回调每行数据
                    }
                }
            }
        } catch (Exception e) {
            log.error("流式API调用异常", e);
            throw e;
        }

        log.info("流式API调用完成");
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
