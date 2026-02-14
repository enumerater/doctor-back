package com.enumerate.disease_detection.POJO.VO.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionCompareVO {
    private List<String> dimensions;
    private List<Integer> regionA;
    private List<Integer> regionB;
}
