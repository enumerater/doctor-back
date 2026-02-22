package com.enumerate.disease_detection.POJO.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

// 防治建议实体类
@Data
@Builder
public class Prevention {
    private List<String> agricultural;   // 农业防治
    private List<String> chemical;       // 化学防治
    private List<String> biological;     // 生物防治
}
