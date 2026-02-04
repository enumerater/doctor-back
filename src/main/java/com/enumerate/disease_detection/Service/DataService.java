package com.enumerate.disease_detection.Service;


import com.enumerate.disease_detection.POJO.VO.DayTemperatureVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.ipc.http.HttpSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DataService {

    // 初始化Jackson的ObjectMapper（全局单例，避免重复创建）
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    // 百度千帆接口地址
    private static final String QIANFAN_URL = "https://qianfan.baidubce.com/v2/ai_search/web_search";
    // 接口授权Token（替换为你的有效Token）
    private static final String AUTHORIZATION = "Bearer bce-v3/ALTAK-opLNWvk7s9CUJC8mPsNIE/2bc5d81f13ae900c1bd22273cd09ad1e9e0371de";

    public String getData(List<Map<String, String>> prompt) throws IOException, URISyntaxException, InterruptedException {
        log.info("request=============================================data正在处理");

        // 1. 构造接口要求的完整请求体（修正参数名+补充必要参数）
        // 外层请求对象：包含必传的messages、search_source，以及可选的过滤参数
        Map<String, Object> requestBody = Map.of(
                "messages", prompt, // 修正：参数名messages（带s），值为前端传递的prompt
                "search_source", "baidu_search_v2", // 必传参数：接口要求的搜索源
                "resource_type_filter", List.of(Map.of("type", "web", "top_k", 20)), // 可选：按文档示例补充
                "search_recency_filter", "year" // 可选：按文档示例补充
        );

        // 2. 将请求对象序列化为【标准JSON字符串】（核心：适配application/json）
        String jsonRequestBody = OBJECT_MAPPER.writeValueAsString(requestBody);
        log.info("请求体JSON：{}", jsonRequestBody); // 可选：打印JSON请求体，方便调试

        // 3. 构建HTTP请求：移除表单构造，直接传递JSON字符串，保留原有头信息
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody)) // 核心修改：JSON字符串作为请求体
                .uri(new URI(QIANFAN_URL))
                .setHeader("Content-Type", "application/json") // 与请求体格式匹配，无需修改
                .setHeader("Authorization", AUTHORIZATION) // 保留你的授权Token
                .build();

        // 4. 发送请求并获取响应（原有逻辑，无修改）
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        log.info("response=============================================data处理完成");
        log.info("response body============================================={}", response.body());

        return response.body();
    }

}
