package com.enumerate.disease_detection.POJO.PO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("long_memory") // MySQL表名
public class LongMemory implements Serializable {
    private Long id;
    // 用户唯一标识
    private Long userId;
    // 作物品种（如番茄、小麦、水稻）
    private String cropType;
    // 种植地域（如华北、山东、南方大棚）
    private String region;
    // 生长阶段（如苗期、结果期、成熟期）
    private String growthStage;
    // 过往病害（JSON字符串，如["番茄早疫病","小麦锈病"]）
    private String pastDisease;
    // 近期农事操作（如浇水频繁、大棚通风少）
    private String farmOperation;
}