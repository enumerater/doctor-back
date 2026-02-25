package com.enumerate.disease_detection.ChatModel;


import com.enumerate.disease_detection.Properties.AiModelProperties;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration  // 让Spring扫描并管理这个类
public class MainModel {

    @Autowired
    private AiModelProperties aiModelProperties;

    @Bean
    public OpenAiChatModel tongYiModel() {
        return OpenAiChatModel.builder()
                .apiKey(aiModelProperties.getTong().getApiKey())
                .baseUrl(aiModelProperties.getTong().getBaseUrl())
                .modelName("qwen-flash")
                .timeout(Duration.ofSeconds(60))
                .maxRetries(3)
                .build();
    }

    @Bean
    public OpenAiEmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(aiModelProperties.getTong().getApiKey())
                .baseUrl(aiModelProperties.getTong().getBaseUrl())
                .modelName("text-embedding-v3")
                .build();
    }

    @Bean
    public OpenAiStreamingChatModel streamingModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(aiModelProperties.getTong().getApiKey())
                .baseUrl(aiModelProperties.getTong().getBaseUrl())
                .modelName("qwen-flash")
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public OpenAiStreamingChatModel deepThinkModel() {
        // 1. 定义深度思考的模型参数（要放在customSettings里）
        Map<String, Object> modelCustomParams = new HashMap<>();
        modelCustomParams.put("think", true); // 通义千问开启深度思考的核心参数

        // 2. 正确配置：用customSettings传递模型参数，而非customHeaders
        return OpenAiStreamingChatModel.builder()
                .apiKey(aiModelProperties.getTong().getApiKey())
                .baseUrl(aiModelProperties.getTong().getBaseUrl())
                .modelName("qwen-plus")
                .timeout(Duration.ofSeconds(120))
                .customParameters(modelCustomParams)
                .build();
    }

    @Bean
    public OpenAiChatModel visionModel() {
        return OpenAiChatModel.builder()
                .apiKey(aiModelProperties.getTong().getApiKey())
                .baseUrl(aiModelProperties.getTong().getBaseUrl())
                .modelName("qwen3-vl-plus") // 视觉模型名称，和Python中一致
                .timeout(Duration.ofSeconds(60)) // 视觉模型响应较慢，延长超时时间
                .maxRetries(3) // 重试次数
                .temperature(0.2) // 核心修改：0.2-0.3是平衡稳定与判断灵活性的黄金值
                .build();
    }


    @Bean
    public OpenAiChatModel qwen3VlStreamingModel() {


        return OpenAiChatModel.builder()
                // 阿里云通义千问 OpenAI 兼容接口地址
                .baseUrl(aiModelProperties.getTong().getBaseUrl())
                // 你的 DASHSCOPE API Key
                .apiKey(aiModelProperties.getTong().getApiKey())
                // 模型名称：直接写 qwen3-vl-plus（新版支持自定义模型名）
                .modelName("qwen3-vl-plus")
                // 超时时间（按需调整）
                .timeout(Duration.ofMinutes(5))
                .build();
    }

}
