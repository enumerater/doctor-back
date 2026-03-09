package com.enumerate.disease_detection.MVC.POJO.VO;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AccumulatedTempPredictionVO {
    private String predictedNextStageDate; // 预计进入下一阶段的日期
    private String nextStageName;           // 下一阶段名称
    private String predictedMatureDate;      // 预计成熟日期
    private List<DailyTempRecordVO> predictionRecords; // 未来预测记录
}
