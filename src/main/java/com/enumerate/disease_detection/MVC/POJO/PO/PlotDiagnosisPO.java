package com.enumerate.disease_detection.MVC.POJO.PO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enumerate.disease_detection.Annotations.ToolName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("plot_diagnosis")
public class PlotDiagnosisPO {

    private String id;
    private String type;
    private String targetId;
    private String title;
    private String content;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String plotId;
    private String createdAt;

}
