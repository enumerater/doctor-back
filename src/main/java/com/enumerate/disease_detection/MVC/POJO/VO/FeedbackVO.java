package com.enumerate.disease_detection.MVC.POJO.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackVO {
    private String id;
    private String diagnosisId;
    private String accuracy;
    private String correctDisease;
    private Integer rating;
    private String comment;
    private String username;
    private String cropType;
    private String diagnosedDisease;
    private String status;
    private String createdAt;
}
