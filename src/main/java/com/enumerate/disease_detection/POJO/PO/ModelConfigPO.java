package com.enumerate.disease_detection.POJO.PO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模型配置实体类，对应数据库模型配置表
 * 存储各类AI模型的基础配置信息
 */
@Data
public class ModelConfigPO {
    /**
     * 模型唯一ID，主键、自增、非空
     */
    private Long id;

    /**
     * 模型编码（如 gpt-4-agri、multimodal-v1），非空、唯一索引
     */
    private String modelCode;

    /**
     * 模型名称（如 “农业 GPT-4”、“多模态病虫害检测模型”），非空
     */
    private String modelName;

    /**
     * 是否有记忆：0 = 否，1 = 是，非空、默认 0
     */
    private String hasMemory;

    /**
     * 是否支持多轮对话：0 = 否，1 = 是，非空、默认 0
     */
    private String supportMultiRound;

    /**
     * 是否支持深度思考：0 = 否，1 = 是，非空、默认 0
     */
    private String supportDeepThinking;

    /**
     * 模型描述（能力、适用场景），可空
     */
    private String description;

    /**
     * 状态：0 = 禁用，1 = 启用，非空、默认 1、索引
     */
    private String status;

    /**
     * 创建时间，非空、默认 CURRENT_TIMESTAMP
     */
    private LocalDateTime createTime;

    /**
     * 更新时间，非空、ON UPDATE CURRENT_TIMESTAMP
     */
    private LocalDateTime updateTime;
}