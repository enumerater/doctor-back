package com.enumerate.disease_detection.POJO.PO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("seasonal_risk")
public class DiseaseSeasonPO {
    private String id;
    private String diseaseId;
    private String diseaseName;
    private String cropName;
    private String month;
    private String riskLevel;
    private String description;

}
