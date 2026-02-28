package com.enumerate.disease_detection.MVC.POJO.DTO;

import lombok.Data;

@Data
public class ChatSessionDTO {
    private Long userId;
    private String sessionTitle;
    private String sessionId;
    private String sessionType;

}
