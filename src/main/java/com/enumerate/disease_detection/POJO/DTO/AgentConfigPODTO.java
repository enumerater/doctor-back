package com.enumerate.disease_detection.POJO.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgentConfigPODTO {
    private String id;
    private String name;
    private String description;
    private Boolean isDefault;

    private String systemPrompt;

    private String responseParams;

    private String agentConfig;

    private String advancedConfig;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String province;
    private String city;
    private String cropTypes;
    private String growthStage;
    private Boolean enableImageAnalysis;
    private Boolean enableFieldManagement;
    private Boolean enablePesticideAdvice;
    private String customPrompt;
    private String enabledSkillIds;
}
