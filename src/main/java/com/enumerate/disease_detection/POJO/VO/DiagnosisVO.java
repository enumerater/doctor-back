package com.enumerate.disease_detection.POJO.VO;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiagnosisVO {
    private String id;
    private String imageUrl;
    private String cropType;
    private Integer hasDisease;
    private String diseaseName;
    private Integer confidence;
    private String severity;
    private String result;

    private String status;
    private String createdAt;

    private String elapsedTime;
    private String plotId;
    private String farmId;
    private String notes;
    private String feedback;
}
