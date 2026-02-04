package com.enumerate.disease_detection.POJO.DTO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("disease_record")
public class DiseaseRecordDTO {
    private Long id;
    private Long userId;
    private String corn_type;
    private String illness_type;

}
