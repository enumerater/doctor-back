package com.enumerate.disease_detection.MVC.POJO.PO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("plot_stage_history")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlotStagePO {
    private Long id;
    private String plotId;
    private String stage;
    private String date;
    private String createdAt;
}
