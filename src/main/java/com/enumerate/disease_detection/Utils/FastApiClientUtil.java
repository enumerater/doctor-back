package com.enumerate.disease_detection.Utils;

import com.enumerate.disease_detection.MVC.POJO.VO.CropDiseaseAnalysisVO;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 修复版：移除受保护的Connection请求头
 */
@Slf4j
public class FastApiClientUtil {
    // 全局HttpClient实例（单例复用）
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20)) // 连接超时10秒
            .build();

    // Python FastAPI服务地址（根据实际部署地址修改）
    private static final String FAST_API_BASE_URL = "http://127.0.0.1:8000/api/analyze-crop";

    /**
     * 调用作物病害识别接口，流式接收结果并封装为VO
     * @param imageUrl 作物图片公网URL
     * @param question 识别问题
     * @return CropDiseaseAnalysisVO
     * @throws Exception 连接/解析异常
     */
    public static CropDiseaseAnalysisVO callCropDiseaseApi(String imageUrl, String question) throws Exception {
        log.info("图像识别+++++++：{}", imageUrl);
        // 1. 参数校验
        if (imageUrl == null || imageUrl.isBlank() || question == null || question.isBlank()) {
            throw new IllegalArgumentException("图片URL和识别问题不能为空");
        }

        // 2. 构建请求URL（URL编码参数）
        String encodedImageUrl = URLEncoder.encode(imageUrl, StandardCharsets.UTF_8);
        String encodedQuestion = URLEncoder.encode(question, StandardCharsets.UTF_8);
        String requestUrl = String.format("%s?image_url=%s&question=%s",
                FAST_API_BASE_URL, encodedImageUrl, encodedQuestion);

        // 3. 构建HttpRequest（关键：移除Connection头，仅保留必要的头）
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .GET()
                // 保留SSE核心头：Accept + 禁用缓存（这两个是允许手动设置的）
                .header("Accept", "text/event-stream")
                .header("Cache-Control", "no-cache")
                // 移除 ↓ 这行受保护的Connection头（核心修复点）
                // .header("Connection", "keep-alive")
                .build();

        // 4. 初始化VO和解析变量
        CropDiseaseAnalysisVO resultVO = new CropDiseaseAnalysisVO();
        StringBuilder thinkingSb = new StringBuilder();
        StringBuilder answerSb = new StringBuilder();
        AtomicBoolean isAnswerStage = new AtomicBoolean(false);
        final String ANSWER_SEPARATOR = "====================完整回复====================";

        // 5. 流式处理响应
        HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofLines()).body()
                .forEach(line -> {
                    if (line == null || line.isBlank()) {
                        return;
                    }
                    if (line.contains(ANSWER_SEPARATOR)) {
                        isAnswerStage.set(true);
                        return;
                    }
                    if (!isAnswerStage.get()) {
                        if (!line.contains("====================思考过程====================")) {
                            thinkingSb.append(line);
                        }
                    } else {
                        answerSb.append(line);
                    }
                });

        // 6. 设置VO内容
        resultVO.setThinkingContent(thinkingSb.toString().trim());
        resultVO.setAnswerContent(answerSb.toString().trim());

        return resultVO;
    }

}
