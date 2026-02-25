package com.enumerate.disease_detection.POJO.PO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("announcement")
public class AnnouncementPO {

    private String id;
    private String title;
    private String content;
    private String type;
    private String priority;
    private String targetUsers;

    @TableField(fill = FieldFill.INSERT)
    private String createdAt;

    private String status;
    private String publishedAt;

}
