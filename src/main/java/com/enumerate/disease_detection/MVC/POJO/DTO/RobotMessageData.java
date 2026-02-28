package com.enumerate.disease_detection.MVC.POJO.DTO;

import lombok.Data;

import java.util.List;

@Data
public class RobotMessageData {
    private List<AgentStep> steps;
    private String finalContent;
}