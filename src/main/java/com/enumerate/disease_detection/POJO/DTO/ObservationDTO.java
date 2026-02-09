package com.enumerate.disease_detection.POJO.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 观察结果DTO
 * 保存ObserverAgent的观察结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObservationDTO {

    /**
     * 是否成功执行
     */
    private Boolean isSuccess;

    /**
     * 提取的数据
     */
    private ExtractedData extractedData;

    /**
     * 发现的问题
     */
    private List<Issue> issues;

    /**
     * 完整性评分：0.0-1.0
     */
    private Double completeness;

    /**
     * 推荐行动：继续 / 重试 / 降级 / 中止
     */
    private String recommendation;

    /**
     * 提取的数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedData {
        /**
         * 作物名称
         */
        private String crop;

        /**
         * 病害名称
         */
        private String disease;

        /**
         * 严重程度：轻度 / 中度 / 重度
         */
        private String severity;

        /**
         * 置信度：0.0-1.0
         */
        private Double confidence;

        /**
         * 其他提取的数据（扩展字段）
         */
        private Map<String, Object> additionalData;
    }

    /**
     * 问题/异常
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Issue {
        /**
         * 类型：warning / error
         */
        private String type;

        /**
         * 问题描述
         */
        private String message;

        /**
         * 严重程度：0-10
         */
        private Integer severity;
    }
}
