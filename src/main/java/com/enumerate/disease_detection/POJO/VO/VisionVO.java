package com.enumerate.disease_detection.POJO.VO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 视觉识别（病害检测）返回VO
 * 与前端可视化展示的数据结构完全对应
 */
@Data
@Builder
public class VisionVO {

    /**
     * 病害名称（如：小麦条锈病、玉米大斑病）
     */
    private String diseaseName;

    /**
     * 病害等级（固定值：轻度/中度/重度）
     */
    private String severityLevel;

    /**
     * 病害等级分数（固定值：1=轻度，2=中度，3=重度）
     */
    private Integer severityScore;

    /**
     * 病害等级描述（对当前病害严重程度的文字说明）
     */
    private String severityDesc;

    /**
     * 用药建议列表
     */
    private List<MedicationVO> medication;

    /**
     * 防治措施列表（每条为一个具体措施）
     */
    private List<String> prevention;

    /**
     * 补充说明（Markdown格式的文本，可选）
     */
    private String supplementaryInfo;

    /**
     * 用药建议子VO
     */
    @Data
    @Builder
    public static class MedicationVO {
        /**
         * 药品名称（如：三唑酮乳油（20%）、多菌灵可湿性粉剂（50%））
         */
        private String name;

        /**
         * 用药剂量（如：1000-1500倍液、800-1000倍液）
         */
        private String dosage;

        /**
         * 使用方法（如：叶面喷雾，每公顷用药液750-900升，间隔7-10天喷一次）
         */
        private String usage;

        /**
         * 注意事项（如：避免高温时段喷施，与其他杀菌剂交替使用以防抗药性）
         */
        private String note;
    }
}