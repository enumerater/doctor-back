package com.enumerate.disease_detection.MVC.POJO.DTO;

import lombok.Data;

@Data
public class AgentMessageDTO {
    private String sessionId;
    private String userMessage;
    private RobotMessageData robotMessage;
}