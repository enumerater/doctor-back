package com.enumerate.disease_detection.POJO.PO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("diagnosis")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosisPO {
    private Long id;
    private Long userId;
    private String imageUrl;
    private String cropType;
    private Integer hasDisease;
    private String diseaseName;

    private String severity;
    private String result;
    private String status;
    private Integer elapsedTime;
    private String plotId;
    private String farmId;
    private String notes;
    private String feedback;


    @TableField(fill = FieldFill.INSERT)
    // 修复3：推荐使用 LocalDateTime 替代 DateTime（兼容 MyBatis 自动映射）
    private String createdAt;

    @TableField(fill = FieldFill.UPDATE)
    private String updatedAt;
}
