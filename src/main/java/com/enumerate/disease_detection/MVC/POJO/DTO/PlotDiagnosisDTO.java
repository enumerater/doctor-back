package com.enumerate.disease_detection.MVC.POJO.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlotDiagnosisDTO {
    private String plotId;
    private String type;
    private String targetId;
    private String title;
    private String content;
}
