package com.enumerate.disease_detection.POJO.PO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("skills")
public class SkillsPO {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private String category;
    private Boolean enabled;
    private String triggers;
    private String params;

}
