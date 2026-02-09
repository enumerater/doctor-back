package com.enumerate.disease_detection.POJO.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 决策结果DTO
 * 保存DecisionAgent的决策结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionDTO {

    /**
     * 决策类型：CONTINUE / RETRY / SKIP / FALLBACK / ABORT / ESCALATE
     */
    private DecisionType decision;

    /**
     * 决策理由
     */
    private String reasoning;

    /**
     * 执行计划调整
     */
    private PlanAdjustments adjustments;

    /**
     * 备用方案
     */
    private FallbackPlan fallbackPlan;

    /**
     * 元数据
     */
    private Metadata metadata;

    /**
     * 决策类型枚举
     */
    public enum DecisionType {
        CONTINUE,    // 继续下一步
        RETRY,       // 重试当前步骤
        SKIP,        // 跳过当前步骤
        FALLBACK,    // 启用备用方案
        ABORT,       // 中止任务
        ESCALATE     // 请求人工介入
    }

    /**
     * 计划调整
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanAdjustments {
        /**
         * 是否修改计划
         */
        private Boolean modifyPlan;

        /**
         * 新增步骤
         */
        private List<Map<String, Object>> newSteps;

        /**
         * 策略变更描述
         */
        private String changeStrategy;
    }

    /**
     * 备用方案
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FallbackPlan {
        /**
         * 是否启用
         */
        private Boolean enabled;

        /**
         * 备用策略描述
         */
        private String strategy;

        /**
         * 备用工具/Agent
         */
        private String fallbackTool;
    }

    /**
     * 元数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        /**
         * 决策置信度：0.0-1.0
         */
        private Double confidence;

        /**
         * 预估影响：high / medium / low
         */
        private String estimatedImpact;

        /**
         * 其他扩展字段
         */
        private Map<String, Object> additionalInfo;
    }
}
