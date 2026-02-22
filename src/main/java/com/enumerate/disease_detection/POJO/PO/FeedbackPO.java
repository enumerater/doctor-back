package com.enumerate.disease_detection.POJO.PO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("feedback")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackPO {
    private Long id;
    private Long userId;
    private String diagnosisId;
    private String accuracy;
    private String correctDisease;
    private Integer rating;
    private String comment;
    private String cropType;
    private String diagnosedDisease;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private String createdAt;
}
