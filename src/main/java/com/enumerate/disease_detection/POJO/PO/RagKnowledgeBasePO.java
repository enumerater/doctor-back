package com.enumerate.disease_detection.POJO.PO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * RAG知识库实体类，对应数据库表 agri_rag_knowledge_base
 * 管理农业专属知识库（如 “小麦种植知识库”“病虫害防治知识库”），是RAG文档的顶层分类
 */
@Data
public class RagKnowledgeBasePO {
    /**
     * 知识库唯一ID，主键、自增、非空
     */
    private Long id;

    /**
     * 知识库名称（如 小麦种植知识库、病虫害防治知识库），非空
     */
    private String kbName;

    /**
     * 知识库编码（如 rag-agri-wheat、rag-agri-disease），非空、唯一索引
     */
    private String kbCode;

    /**
     * 知识库类型：种植技术/病虫害防治/农资使用/政策法规，可空、索引
     */
    private String kbType;

    /**
     * 知识库描述（适用范围、数据来源），可空
     */
    private String description;

    /**
     * 知识库负责人（管理员）ID，关联 sys_user.id，非空、外键
     * 约束：ON DELETE RESTRICT（删除管理员时限制）、ON UPDATE CASCADE（更新管理员ID时级联）
     */
    private Long adminId;

    /**
     * 状态：0=禁用（不参与检索），1=启用，非空、默认 1、索引
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

    /**
     * 软删除：0=未删除，1=已删除，非空、默认 0、索引
     */
    private String deleted;
}