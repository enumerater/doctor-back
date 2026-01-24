package com.enumerate.disease_detection.POJO.PO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * RAG文档分块实体类，对应数据库表 agri_rag_doc_chunk
 * 存储文档解析后的文本分块（RAG检索的最小单元），是RAG检索的核心数据源
 */
@Data
public class RagDocChunkPO {
    /**
     * 分块唯一ID，主键、自增、非空
     */
    private Long id;

    /**
     * 所属文档ID，关联 agri_rag_document.id，非空、外键
     * 约束：ON DELETE RESTRICT（删除文档时限制）、ON UPDATE CASCADE（更新文档ID时级联）
     */
    private Long docId;

    /**
     * 分块文本内容（核心检索内容），非空、全文索引（适配关键词检索）
     */
    private String chunkContent;

    /**
     * 分块在文档中的序号（如第1块、第2块），非空
     */
    private Integer chunkIndex;

    /**
     * 分块关键词（逗号分隔，如 小麦,白粉病,防治），可空
     */
    private String keywords;

    /**
     * 向量ID（对接向量数据库，如Milvus/FAISS，非必需），可空
     */
    private String vectorId;

    /**
     * 创建时间，非空、默认 CURRENT_TIMESTAMP
     */
    private LocalDateTime createTime;

    /**
     * 软删除：0=未删除，1=已删除，非空、默认 0
     */
    private String deleted;
}