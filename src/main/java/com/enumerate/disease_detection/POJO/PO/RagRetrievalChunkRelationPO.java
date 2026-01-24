package com.enumerate.disease_detection.POJO.PO;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * RAG检索-分块关联实体类，对应数据库表 agri_rag_retrieval_chunk_relation
 * 记录单次检索返回的具体分块（一对多：一次检索→多条分块），完善RAG检索链路的追溯
 */
@Data
public class RagRetrievalChunkRelationPO {
    /**
     * 关联ID，主键、自增、非空
     */
    private Long id;

    /**
     * 检索记录ID，关联 agri_rag_retrieval_record.id，非空、外键
     * 约束：ON DELETE RESTRICT（删除检索记录时限制）、ON UPDATE CASCADE（更新检索记录ID时级联）
     */
    private Long retrievalId;

    /**
     * 分块ID，关联 agri_rag_doc_chunk.id，非空、外键
     * 约束：ON DELETE RESTRICT（删除分块时限制）、ON UPDATE CASCADE（更新分块ID时级联）
     */
    private Long chunkId;

    /**
     * 相似度（向量检索时：0-100，如95.50；关键词检索可填100），可空
     */
    private BigDecimal similarity;

    /**
     * 创建时间，非空、默认 CURRENT_TIMESTAMP
     */
    private LocalDateTime createTime;

    /**
     * 【业务约束】retrieval_id + chunk_id 组合为唯一索引（uk_retrieval_chunk），避免同一检索记录重复关联同一分块
     */
    private transient String uniqueKeyRemark; // 仅注释用，无数据库映射
}