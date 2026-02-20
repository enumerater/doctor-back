package com.enumerate.disease_detection.POJO.PO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("diseases")
public class DiseasesPO {
    private String id;
    private String name;
    private String cropName;
    private String category;
    private String thumbnail;
    private String severity;

    private String symptomsText;
    private String conditionsTemperature;
    private String conditionsHumidity;
    private String conditionsSeason;
    private String conditionsStage;

    private String transmission;

    private String preventionAgricultural;
    private String preventionChemical;
    private String preventionBiological;

}
