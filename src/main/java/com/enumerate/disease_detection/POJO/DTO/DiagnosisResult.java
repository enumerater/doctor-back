package com.enumerate.disease_detection.POJO.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

// 诊断结果的主实体类
@Data
@Builder
public class DiagnosisResult {
    private Boolean hasDisease;          // 是否患病
    private String healthyDesc;          // 健康描述（null则不序列化）
    private String diseaseName;          // 病害名称
    private Integer confidence;          // 置信度
    private String severity;             // 严重程度
    private List<String> symptoms;       // 症状列表
    private Prevention prevention;       // 防治建议
    private List<String> notes;          // 备注
}
