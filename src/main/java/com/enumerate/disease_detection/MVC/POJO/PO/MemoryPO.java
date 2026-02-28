package com.enumerate.disease_detection.MVC.POJO.PO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("memory")
public class MemoryPO {
    private Long id;
    private String message;
    private String unionId;
}
