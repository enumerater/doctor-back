package com.enumerate.disease_detection.POJO.VO.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapMarkerVO {
    private Integer id;
    private String type;
    private Double lng;
    private Double lat;
    private String name;
    private String description;
    private Integer severity;
    private String crop;
}
