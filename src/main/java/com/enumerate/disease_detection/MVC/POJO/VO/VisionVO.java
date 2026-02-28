package com.enumerate.disease_detection.MVC.POJO.VO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VisionVO {
    /**
     * 核心标识：是否检测到病害（true=有病害，false=健康/无病害）
     * 大模型无病害时，仅需将该字段设为false，其余病害字段可置空
     */
    private Boolean hasDisease;

    /**
     * 健康描述（可选）：无病害时的说明（如：图片中作物生长健康，未检测到任何病害）
     * 有病害时该字段可置空
     */
    private String healthyDesc;

    /**
     * 原有字段：病害名称（如：小麦白粉病）
     * 无病害时置空
     */
    private String diseaseName;

    /**
     * 原有字段：识别置信度（百分比，如：95）
     * 无病害时置空
     */
    private Integer confidence;

    /**
     * 原有字段：病害等级（轻微/中度/重度）
     * 无病害时置空
     */
    private String severity;

    /**
     * 原有字段：病害症状列表
     * 无病害时置空
     */
    private List<String> symptoms;

    /**
     * 原有字段：防治方法
     * 无病害时置空
     */
    private Prevention prevention;

    /**
     * 原有字段：注意事项列表
     * 无病害时置空
     */
    private List<String> notes;

    /**
     * 原有子VO：防治方法
     */
    @Data
    @Builder
    public static class Prevention {
        private List<String> agricultural;
        private List<String> chemical;
        private List<String> biological;
    }
}