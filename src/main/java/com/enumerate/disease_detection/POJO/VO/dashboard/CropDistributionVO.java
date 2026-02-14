package com.enumerate.disease_detection.POJO.VO.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropDistributionVO {
    private String name;
    private Integer value;
}
