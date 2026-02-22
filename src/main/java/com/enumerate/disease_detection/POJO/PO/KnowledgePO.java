package com.enumerate.disease_detection.POJO.PO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("knowledge")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgePO {
    private Long id;
    private String name;
    private String crop;
    private String category;
    private String symptoms;
    private String treatment;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private String createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedAt;
}
