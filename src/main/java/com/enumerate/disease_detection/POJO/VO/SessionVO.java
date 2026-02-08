package com.enumerate.disease_detection.POJO.VO;

import lombok.Data;

@Data
public class SessionVO {
    private Long id;
    private String sessionTitle;
    private String lastChatTime;
    private String sessionStatus;
    private String sessionId;
    private String sessionType;

}
