package com.enumerate.disease_detection.POJO.VO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SkillsVO {
    private List<String> skills;
    private List<String> enabledSkillIds;
}
