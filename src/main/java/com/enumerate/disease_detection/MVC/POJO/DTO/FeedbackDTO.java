package com.enumerate.disease_detection.MVC.POJO.DTO;

import lombok.Data;

@Data
public class FeedbackDTO {
    private String diagnosisId;
    private String accuracy;
    private String correctDisease;
    private Integer rating;
    private String comment;
    private String cropType;
    private String diagnosedDisease;
}
