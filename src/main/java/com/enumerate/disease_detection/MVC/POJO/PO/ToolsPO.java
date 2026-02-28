package com.enumerate.disease_detection.MVC.POJO.PO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("skills")
public class ToolsPO {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private String category;
    private Boolean enabled;
    private String triggers;
    private String params;

}
