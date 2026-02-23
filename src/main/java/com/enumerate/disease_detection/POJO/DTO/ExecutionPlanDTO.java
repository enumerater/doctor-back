package com.enumerate.disease_detection.POJO.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 执行计划DTO
 * 保存PlannerAgent生成的执行计划
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutionPlanDTO {

    /**
     * 任务类型：图像诊断 / 病害咨询 / 混合任务 / 一般咨询
     */
    private String taskType;

    /**
     * 任务复杂度：简单 / 中等 / 复杂
     */
    private String complexity;

    /**
     * 计划置信度：0.0-1.0
     */
    private Double confidence;

    /**
     * 执行步骤列表
     */
    private List<ExecutionStep> steps;

    /**
     * 最大迭代次数
     */
    private Integer maxIterations;

    /**
     * 需要的Tool名称列表
     */
    private List<String> toolsNeeded;

    /**
     * 备用策略
     */
    private String fallbackStrategy;

    /**
     * 执行步骤
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionStep {
        /**
         * 步骤序号
         */
        private Integer step;

        /**
         * 动作描述
         */
        private String action;

        /**
         * 使用的工具/Agent
         */
        private String tool;

        /**
         * 优先级：high / medium / low
         */
        private String priority;

        /**
         * 是否为关键步骤（失败需中止）
         */
        private Boolean critical;
    }
}
