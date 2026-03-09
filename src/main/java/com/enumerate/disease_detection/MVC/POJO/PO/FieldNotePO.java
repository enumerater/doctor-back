package com.enumerate.disease_detection.MVC.POJO.PO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("field_note")
public class FieldNotePO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long plotId;
    private String content;
    private String images; // JSON string
    private LocalDate date;
    private Boolean isAiGenerated;
    private String weatherInfo;
}
