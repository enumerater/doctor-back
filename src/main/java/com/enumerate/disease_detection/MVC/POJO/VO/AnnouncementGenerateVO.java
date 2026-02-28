package com.enumerate.disease_detection.MVC.POJO.VO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementGenerateVO {
    private String title;
    private String content;
    private String type;
    private String priority;
}
