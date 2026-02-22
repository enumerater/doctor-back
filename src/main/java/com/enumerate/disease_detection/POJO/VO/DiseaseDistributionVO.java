package com.enumerate.disease_detection.POJO.VO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiseaseDistributionVO {
    private String name;
    private Integer count;
}
