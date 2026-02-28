package com.enumerate.disease_detection.MVC.POJO.PO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Data
@TableName("notification")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationPO {
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String content;
    @TableField("is_readd")
    private Boolean isRead;
    private String link;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private String priority;
    private String announcementId;
}
