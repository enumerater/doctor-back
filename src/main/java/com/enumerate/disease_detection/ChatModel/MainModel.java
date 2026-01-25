package com.enumerate.disease_detection.ChatModel;

import com.enumerate.disease_detection.Properties.AiModelProperties;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
//                .modelName("qwen-max")
                .modelName("qwen-flash")
                .timeout(Duration.ofSeconds(3))
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
                .maxRetries(1) // 重试次数
                .build();
    }

}
