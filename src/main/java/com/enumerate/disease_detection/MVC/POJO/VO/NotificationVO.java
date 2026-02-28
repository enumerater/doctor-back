package com.enumerate.disease_detection.MVC.POJO.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationVO {
    private String id;
    private String type;
    private String title;
    private String content;
    private String priority;
    private Boolean read;
    private LocalDateTime createdAt;
    private String link;
}
