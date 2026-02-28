package com.enumerate.disease_detection.MVC.POJO.VO;

import lombok.Data;

@Data
public class CropDiseaseAnalysisVO {
    /** 通义千问的思考推理过程 */
    private String thinkingContent;
    /** 最终的作物/病害识别完整回复 */
    private String answerContent;
}