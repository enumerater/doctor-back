package com.enumerate.disease_detection.MVC.POJO.DTO;

import lombok.Data;

@Data
public class AgentStep {
    private String type;      // 步骤类型：status, think, final_result等
    private String content;   // 步骤内容
    private String status;    // 状态：processing, completed（可选）
    private String timestamp; // 时间戳
}