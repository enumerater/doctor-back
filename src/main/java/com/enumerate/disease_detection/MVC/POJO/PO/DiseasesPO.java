package com.enumerate.disease_detection.MVC.POJO.PO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("crop_disease")
public class DiseasesPO {
    private String id;
    private String category;
    private String cropName;
    private String diseaseName;
    private String introduction;
    private String pics;
    private String symbol;

    private String factor;
    private String prevention;
    private String englishName;
    private String chineseName;

}
