package com.enumerate.disease_detection.POJO.VO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FarmDetailVO {
    private String id;
    private String userId;
    private String name;
    private String location;

    private String area;

    // 修复2：plotCount 是数量，改为 Integer 类型
    private String plotCount;

    private List<PlotVO> plots;
}
