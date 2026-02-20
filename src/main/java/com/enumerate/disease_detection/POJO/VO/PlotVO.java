package com.enumerate.disease_detection.POJO.VO;
import com.enumerate.disease_detection.POJO.PO.PlotStagePO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PlotVO {
    private String id;
    private String farmId;
    private String name;
    private String cropType;
    private String area;
    private String sowingDate;
    private String soilType;
    private String growthStage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<PlotStagePO> plotStagePOList;


}
