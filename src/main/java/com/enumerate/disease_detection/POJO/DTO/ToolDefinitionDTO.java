package com.enumerate.disease_detection.POJO.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Tool定义DTO
 * 用于封装从数据库加载的Tool配置信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolDefinitionDTO {
    /**
     * Tool ID
     */
    private Long id;

    /**
     * Tool名称（用于工具标识）
     */
    private String name;

    /**
     * Tool描述（提供给LLM理解工具用途）
     */
    private String description;

    /**
     * Tool分类（vision, calculation, query, weather等）
     */
    private String category;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 触发关键词列表（用于LLM判断何时使用该工具）
     */
    private List<String> triggers;

    /**
     * API端点配置
     */
    private ApiEndpointConfig apiConfig;

    /**
     * API端点配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiEndpointConfig {
        /**
         * FastAPI端点路径
         */
        private String endpoint;

        /**
         * HTTP方法（GET/POST）
         */
        private String method;

        /**
         * 参数列表
         */
        private List<ParamDefinition> params;

        /**
         * 超时时间（秒）
         */
        private Integer timeout;
    }

    /**
     * 参数定义
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParamDefinition {
        /**
         * 参数名称
         */
        private String name;

        /**
         * 参数类型（string, number, boolean, image_url等）
         */
        private String type;

        /**
         * 参数描述
         */
        private String description;

        /**
         * 是否必填
         */
        private Boolean required;

        /**
         * 默认值
         */
        private String defaultValue;
    }
}
