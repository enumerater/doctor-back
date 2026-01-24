package com.enumerate.disease_detection.POJO.PO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * RAG检索记录实体类，对应数据库表 agri_rag_retrieval_record
 * 记录用户/大模型的RAG检索行为，追溯“某条对话消息的回答来源于哪些RAG分块”，是RAG可解释性的核心表
 */
@Data
public class RagRetrievalRecordPO {
    /**
     * 检索记录唯一ID，主键、自增、非空
     */
    private Long id;

    /**
     * 检索用户ID，关联 sys_user.id，非空、外键
     * 约束：ON DELETE RESTRICT（删除用户时限制）、ON UPDATE CASCADE（更新用户ID时级联）
     */
    private Long userId;

    /**
     * 所属对话会话ID，关联 agri_chat_session.id，非空、外键
     * 约束：ON DELETE RESTRICT（删除会话时限制）、ON UPDATE CASCADE（更新会话ID时级联）
     */
    private Long sessionId;

    /**
     * 所属对话消息ID，关联 agri_chat_message.id，非空、外键
     * 约束：ON DELETE RESTRICT（删除消息时限制）、ON UPDATE CASCADE（更新消息ID时级联）
     */
    private Long messageId;

    /**
     * 检索关键词（如 小麦白粉病防治），非空、索引（按关键词统计检索频次）
     */
    private String retrievalKeyword;

    /**
     * 检索的知识库ID，关联 agri_rag_knowledge_base.id，非空、外键
     * 约束：ON DELETE RESTRICT（删除知识库时限制）、ON UPDATE CASCADE（更新知识库ID时级联）
     */
    private Long kbId;

    /**
     * 检索类型：0=关键词检索，1=向量检索，非空、默认 0
     */
    private String retrievalType;

    /**
     * 检索时间，非空、默认 CURRENT_TIMESTAMP
     */
    private LocalDateTime retrievalTime;

    /**
     * 检索返回的分块数量，非空、默认 0
     */
    private Integer retrievalCount;

    /**
     * 创建时间，非空、默认 CURRENT_TIMESTAMP
     */
    private LocalDateTime createTime;

    /**
     * 软删除：0=未删除，1=已删除，非空、默认 0、索引
     */
    private String deleted;
}