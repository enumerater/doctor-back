package com.enumerate.disease_detection.POJO.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DiagnosisDTO {
    private String imageUrl;
    private String cropType;
    private Integer hasDisease;
    private String diseaseName;
    private Integer confidence;
    private String severity;
    private String result;
    private String status;
    private Integer elapsedTime;
    private String createdAt;
}


