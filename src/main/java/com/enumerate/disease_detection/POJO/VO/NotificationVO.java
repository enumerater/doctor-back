package com.enumerate.disease_detection.POJO.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationVO {
    private String id;
    private String type;
    private String title;
    private String content;
    private Boolean read;
    private String createdAt;
    private String link;
}
