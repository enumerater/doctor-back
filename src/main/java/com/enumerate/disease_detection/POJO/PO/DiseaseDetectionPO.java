package com.enumerate.disease_detection.POJO.PO;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 病虫害检测记录实体类，对应数据库表 agri_disease_detection
 * 存储用户上传图片进行病虫害检测的全流程记录
 */
@Data
public class DiseaseDetectionPO {
    /**
     * 检测记录唯一ID，主键、自增、非空
     */
    private Long id;

    /**
     * 检测用户ID，关联 sys_user.id，非空、外键、索引
     */
    private Long userId;

    /**
     * 检测图片存储URL（建议OSS/云存储），非空
     */
    private String imageUrl;

    /**
     * 图片原始名称，可空
     */
    private String imageName;

    /**
     * 多模态模型ID，关联 agri_model_config.id，非空、外键、索引
     */
    private Long modelId;

    /**
     * 检测结果（病虫害名称、概率、防治建议），非空
     */
    private String detectionResult;

    /**
     * 检测置信度（0-100，如 98.50），可空
     */
    private BigDecimal confidence;

    /**
     * 检测时间，非空、默认 CURRENT_TIMESTAMP
     */
    private LocalDateTime detectionTime;

    /**
     * 检测状态：0 = 检测中，1 = 完成，2 = 失败，非空、默认 0、索引
     */
    private String status;

    /**
     * 失败时的错误信息，可空
     */
    private String errorMsg;

    /**
     * 软删除：0 = 未删除，1 = 已删除，非空、默认 0、索引
     */
    private String deleted;
}