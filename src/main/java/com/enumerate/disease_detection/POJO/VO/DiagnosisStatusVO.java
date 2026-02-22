package com.enumerate.disease_detection.POJO.VO;

import com.enumerate.disease_detection.POJO.VO.dashboard.CropDistributionVO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DiagnosisStatusVO {
    private String total;
    private String diseased;
    private String healthy;

    private List<CropDistribution> cropDistribution;

    private List<DiseaseDistributionVO> diseaseDistributionVO;

}

