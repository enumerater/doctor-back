package com.enumerate.disease_detection.POJO.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Tool调用计划DTO
 * ToolAgent返回的调用计划
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallPlanDTO {
    /**
     * 是否需要调用Tool
     */
    private Boolean needSkill;

    /**
     * 要调用的Tool名称
     */
    private String skillName;

    /**
     * 调用原因/推理过程
     */
    private String reasoning;

    /**
     * 调用参数
     */
    private Map<String, Object> parameters;
}
