package com.enumerate.disease_detection.ChatModel;

import com.enumerate.disease_detection.Properties.AiModelProperties;
import dev.langchain4j.model.chat.StreamingChatModel;
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

    /**
     * 1. 创建一个强制使用 HTTP/1.1 的通用客户端底座 (免疫假死防断连)
     */
    @Bean
    public dev.langchain4j.http.client.HttpClientBuilder langchainHttpClientBuilder() {
        return dev.langchain4j.http.client.jdk.JdkHttpClient.builder()
                .httpClientBuilder(
                        java.net.http.HttpClient.newBuilder()
                                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                );
    }

    /**
     * 2. 普通同步模型
     */
    @Bean
    public OpenAiChatModel tongYiModel(dev.langchain4j.http.client.HttpClientBuilder httpClientBuilder) {
        return OpenAiChatModel.builder()
                .apiKey(aiModelProperties.getTong().getApiKey())
                .baseUrl(aiModelProperties.getTong().getBaseUrl())
                .modelName("qwen-flash")
                .timeout(Duration.ofSeconds(60))
                .maxRetries(3)
                .httpClientBuilder(httpClientBuilder) // 注入！
                .build();
    }

    @Bean
    public OpenAiChatModel deepseekModel(dev.langchain4j.http.client.HttpClientBuilder httpClientBuilder) {
        return OpenAiChatModel.builder()
                .apiKey(aiModelProperties.getDeepseek().getApiKey())
                .baseUrl(aiModelProperties.getDeepseek().getBaseUrl())
                .modelName(aiModelProperties.getDeepseek().getModelNameDeepseekChat())
                .timeout(Duration.ofSeconds(60))
                .maxRetries(3)
                .httpClientBuilder(httpClientBuilder) // 注入！
                .build();
    }


    /**
     * 3. 流式模型
     */
    //qwen-flash
    @Bean
    public dev.langchain4j.model.chat.StreamingChatModel tongYiStreamingModel(dev.langchain4j.http.client.HttpClientBuilder httpClientBuilder) {
        return OpenAiStreamingChatModel.builder()
                .apiKey(aiModelProperties.getTong().getApiKey())
                .baseUrl(aiModelProperties.getTong().getBaseUrl())
                .modelName("qwen-flash")
                .timeout(Duration.ofSeconds(60))
                .httpClientBuilder(httpClientBuilder) // 注入！
                .build();
    }

    //qwen3.5-plus
    @Bean
    public dev.langchain4j.model.chat.StreamingChatModel tongYiPlusStreamingModel(dev.langchain4j.http.client.HttpClientBuilder httpClientBuilder) {
        return OpenAiStreamingChatModel.builder()
                .apiKey(aiModelProperties.getTong().getApiKey())
                .baseUrl(aiModelProperties.getTong().getBaseUrl())
                .modelName("qwen3.5-plus")
                .timeout(Duration.ofSeconds(60))
                .httpClientBuilder(httpClientBuilder) // 注入！
                .build();
    }

    //qwen3.5-flash
    @Bean
    public dev.langchain4j.model.chat.StreamingChatModel tongYiFlashStreamingModel(dev.langchain4j.http.client.HttpClientBuilder httpClientBuilder) {
        return OpenAiStreamingChatModel.builder()
                .apiKey(aiModelProperties.getTong().getApiKey())
                .baseUrl(aiModelProperties.getTong().getBaseUrl())
                .modelName("qwen3.5-flash")
                .timeout(Duration.ofSeconds(60))
                .httpClientBuilder(httpClientBuilder) // 注入！
                .build();
    }



    //glm-5
    @Bean
    public dev.langchain4j.model.chat.StreamingChatModel GlmStreamingModel(dev.langchain4j.http.client.HttpClientBuilder httpClientBuilder) {
        return OpenAiStreamingChatModel.builder()
                .apiKey(aiModelProperties.getTong().getApiKey())
                .baseUrl(aiModelProperties.getTong().getBaseUrl())
                .modelName("glm-5")
                .timeout(Duration.ofSeconds(60))
                .httpClientBuilder(httpClientBuilder) // 注入！
                .build();
    }

    //DeepSeek-V3.2
    @Bean
    public StreamingChatModel deepseekStreamingModel(dev.langchain4j.http.client.HttpClientBuilder httpClientBuilder) {
        return OpenAiStreamingChatModel.builder()
                .apiKey(aiModelProperties.getDeepseek().getApiKey())
                .baseUrl(aiModelProperties.getDeepseek().getBaseUrl())
                .modelName(aiModelProperties.getDeepseek().getModelNameDeepseekChat())
                .timeout(Duration.ofSeconds(60))
                .httpClientBuilder(httpClientBuilder) // 注入！
                .build();
    }

    /**
     * 4. 向量化模型 (Embedding)
     */
    @Bean
    public OpenAiEmbeddingModel embeddingModel(dev.langchain4j.http.client.HttpClientBuilder httpClientBuilder) {
        return OpenAiEmbeddingModel.builder()
                .apiKey(aiModelProperties.getTong().getApiKey())
                .baseUrl(aiModelProperties.getTong().getBaseUrl())
                .modelName("text-embedding-v3")
                .httpClientBuilder(httpClientBuilder) // 注入！防止生成向量时卡死
                .build();
    }

    /**
     * 5. 深度思考模型 (流式)
     */
    @Bean
    public OpenAiStreamingChatModel deepThinkModel(dev.langchain4j.http.client.HttpClientBuilder httpClientBuilder) {
        Map<String, Object> modelCustomParams = new HashMap<>();
        modelCustomParams.put("think", true);

        return OpenAiStreamingChatModel.builder()
                .apiKey(aiModelProperties.getTong().getApiKey())
                .baseUrl(aiModelProperties.getTong().getBaseUrl())
                .modelName("qwen-plus")
                .timeout(Duration.ofSeconds(120))
                .customParameters(modelCustomParams)
                .httpClientBuilder(httpClientBuilder) // 注入！
                .build();
    }

    /**
     * 6. 视觉模型 (同步)
     */
    @Bean
    public OpenAiChatModel visionModel(dev.langchain4j.http.client.HttpClientBuilder httpClientBuilder) {
        return OpenAiChatModel.builder()
                .apiKey(aiModelProperties.getTong().getApiKey())
                .baseUrl(aiModelProperties.getTong().getBaseUrl())
                .modelName("qwen3-vl-plus")
                .timeout(Duration.ofSeconds(60))
                .maxRetries(3)
                .temperature(0.2)
                .httpClientBuilder(httpClientBuilder) // 注入！
                .build();
    }

    /**
     * 7. 视觉模型 (流式 - 虽然你这里返回的是 OpenAiChatModel，但我保留了你的命名)
     */
    @Bean
    public OpenAiChatModel qwen3VlStreamingModel(dev.langchain4j.http.client.HttpClientBuilder httpClientBuilder) {
        return OpenAiChatModel.builder()
                .baseUrl(aiModelProperties.getTong().getBaseUrl())
                .apiKey(aiModelProperties.getTong().getApiKey())
                .modelName("qwen3-vl-plus")
                .timeout(Duration.ofMinutes(5))
                .httpClientBuilder(httpClientBuilder) // 注入！
                .build();
    }
}