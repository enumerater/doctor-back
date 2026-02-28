package com.enumerate.disease_detection.MVC.POJO.PO;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("plot")
@NoArgsConstructor  // 必须添加无参构造器
@AllArgsConstructor // 显式声明全参构造器
public class PlotPO {
    private Long id;
    private Long farmId;
    private String name;
    private String cropType;
    private String area;
    private String sowingDate;
    private String soilType;
    private String growthStage;

    @TableField(fill = FieldFill.INSERT)
    // 修复3：推荐使用 LocalDateTime 替代 DateTime（兼容 MyBatis 自动映射）
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;


}
