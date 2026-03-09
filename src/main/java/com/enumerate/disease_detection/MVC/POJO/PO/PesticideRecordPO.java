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
@TableName("pesticide_record")
public class PesticideRecordPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long plotId;
    private String medicineName;
    private String category;
    private Float dosage;
    private String unit;
    private Integer ratio;
    private Float totalVolume;
    private String purpose;
    private LocalDate applicationDate;
    private Integer effectEvaluation;
    private String effectRemarks;
    private Float applicationArea;
}
