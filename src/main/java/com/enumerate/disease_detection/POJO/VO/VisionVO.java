package com.enumerate.disease_detection.POJO.VO;

import lombok.Builder;
import lombok.Data;

import java.util.List;


/**
 * 视觉识别结果VO（返回给前端的结构化数据）
 */
@Data
@Builder
public class VisionVO {
    /**
     * 病害名称（如：小麦白粉病）
     */
    private String diseaseName;

    /**
     * 识别置信度（百分比，如：95）
     */
    private Integer confidence;

    /**
     * 病害等级（轻微/中度/重度）
     */
    private String severity;

    /**
     * 病害症状列表
     */
    private List<String> symptoms;

    /**
     * 防治方法
     */
    private Prevention prevention;

    /**
     * 注意事项列表
     */
    private List<String> notes;

    /**
     * 防治方法子VO
     */
    @Data
    @Builder
    public static class Prevention {
        /**
         * 农业防治
         */
        private List<String> agricultural;

        /**
         * 化学防治
         */
        private List<String> chemical;

        /**
         * 生物防治
         */
        private List<String> biological;
    }


}