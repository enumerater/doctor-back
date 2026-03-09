package com.enumerate.disease_detection.MVC.POJO.VO;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AccumulatedTempVO {
    private Double currentTemp;      // 当前累计有效积温
    private Double targetTemp;       // 当前生长阶段所需的总积温目标
    private List<DailyTempRecordVO> records; // 历史每日积温记录
}
