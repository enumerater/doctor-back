package com.enumerate.disease_detection.POJO.VO.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionSummaryVO {
    private Integer totalDiagnosis;
    private Integer todayDiagnosis;
    private Integer diseaseTypes;
    private Integer monitorStations;
    private Double aiAccuracy;
    private Integer alertCount;
}
