package com.enumerate.disease_detection.POJO.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Skill调用计划DTO
 * SkillAgent返回的调用计划
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillCallPlanDTO {
    /**
     * 是否需要调用Skill
     */
    private Boolean needSkill;

    /**
     * 要调用的Skill名称
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
