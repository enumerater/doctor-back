package com.enumerate.disease_detection.POJO.VO.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseAlertVO {
    private Integer id;
    private String disease;
    private String location;
    private String level;
    private Integer area;
    private String time;
    private Double lng;
    private Double lat;
    private String trend;
}
