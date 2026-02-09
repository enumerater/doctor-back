package com.enumerate.disease_detection.POJO.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 反思结果DTO
 * 保存ReflectorAgent的反思评估结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReflectionDTO {

    /**
     * 总体质量评分：0.0-1.0
     */
    private Double overallScore;

    /**
     * 各维度评分
     */
    private QualityScores scores;

    /**
     * 是否需要重试
     */
    private Boolean needsRetry;

    /**
     * 根因分析
     */
    private String rootCause;

    /**
     * 改进建议列表
     */
    private List<String> suggestions;

    /**
     * 下一步行动：继续 / 重试当前步骤 / 降级处理 / 请求人工
     */
    private String nextAction;

    /**
     * 决策理由
     */
    private String reasoning;

    /**
     * 质量评分（多维度）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityScores {
        /**
         * 准确性：0.0-1.0
         */
        private Double accuracy;

        /**
         * 完整性：0.0-1.0
         */
        private Double completeness;

        /**
         * 一致性：0.0-1.0
         */
        private Double consistency;

        /**
         * 可操作性：0.0-1.0
         */
        private Double actionability;
    }
}
