package com.enumerate.disease_detection.Properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大模型配置类（绑定application.yml中的ai.model配置）
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.model")
public class AiModelProperties {
    // 默认使用的模型类型
    private String defaultType;
    // 通用参数
    private CommonConfig common;
    // 通义千问模型配置
    private TongYi tong;

    private Deepseek deepseek;

    // 通用配置内部类
    @Data
    public static class CommonConfig {
        private Double temperature;
        private Integer maxTokens;
    }

    // 通义千问配置内部类
    @Data
    public static class TongYi {
        private String apiKey;
        private String baseUrl;
        private String modelName;
    }

    @Data
    public static class Deepseek {
        private String apiKey;
        private String baseUrl;
        private String modelNameDeepseekChat;
        private String modelNameDeepseekReasoner;
    }

}