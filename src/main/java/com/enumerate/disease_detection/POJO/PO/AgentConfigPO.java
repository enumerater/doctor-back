package com.enumerate.disease_detection.POJO.PO;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enumerate.disease_detection.Utils.ObjectToStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent配置实体类，对应数据库表 sys_agent_config
 * 管理端专用，存储Agent的基础配置信息
 */
@Data
@TableName("agent_configs")
@Builder
public class AgentConfigPO {
   private Long id;
   private  Long userId;
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