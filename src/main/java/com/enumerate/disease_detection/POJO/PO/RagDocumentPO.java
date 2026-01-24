package com.enumerate.disease_detection.POJO.PO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * RAG文档实体类，对应数据库表 agri_rag_document
 * 存储知识库下的具体文档（如 PDF、Word、Txt、网页爬取的农业技术文档），是RAG的原始数据载体
 */
@Data
public class RagDocumentPO {
    /**
     * 文档唯一ID，主键、自增、非空
     */
    private Long id;

    /**
     * 所属知识库ID，关联 agri_rag_knowledge_base.id，非空、外键
     * 约束：ON DELETE RESTRICT（删除知识库时限制）、ON UPDATE CASCADE（更新知识库ID时级联）
     */
    private Long kbId;

    /**
     * 文档名称（如 小麦白粉病防治技术手册.pdf），非空
     */
    private String docName;

    /**
     * 文档类型：pdf/txt/word/html/markdown，非空
     */
    private String docType;

    /**
     * 文档存储URL（OSS/本地路径），非空
     */
    private String docUrl;

    /**
     * 文档大小（字节），可空
     */
    private Long docSize;

    /**
     * 上传人（管理员）ID，关联 sys_user.id，非空、外键
     * 约束：ON DELETE RESTRICT（删除管理员时限制）、ON UPDATE CASCADE（更新管理员ID时级联）
     */
    private Long uploadUserId;

    /**
     * 解析状态：0=未解析，1=解析完成，2=解析失败，非空、默认 0、索引
     */
    private String parseStatus;

    /**
     * 解析失败错误信息，可空
     */
    private String parseErrorMsg;

    /**
     * 上传时间，非空、默认 CURRENT_TIMESTAMP
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