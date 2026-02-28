package com.enumerate.disease_detection.MVC.POJO.PO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("crops")
public class CropsPO {
    private String id;
    private String name;
    private String icon;
}
