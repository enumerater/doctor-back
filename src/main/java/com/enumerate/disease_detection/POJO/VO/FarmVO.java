package com.enumerate.disease_detection.POJO.VO;

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
public class FarmVO {
    private String id;
    private String userId;
    private String name;
    private String location;

    private String area;

    // 修复2：plotCount 是数量，改为 Integer 类型
    private String plotCount;
}