package com.enumerate.disease_detection.MVC.POJO.PO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 对话消息实体类，对应数据库表 agri_chat_message
 * 存储会话内的每条消息，支持多轮上下文关联和深度思考记录
 */
@Data
@TableName("chat_message")
@Builder
public class ChatMessagePO {
    /**
     * 消息唯一ID，主键、自增、非空
     */
    private Long id;

    /**
     * 所属会话ID，关联 agri_chat_session.id，非空、外键、索引
     */
    private String sessionId;

    /**
     * 消息角色：0 = 用户，1 = 模型助手，非空、索引
     */
    private String messageRole;

    /**
     * 消息内容（文本），非空
     */
    private String messageContent;

    /**
     * 发送时间，非空、默认 CURRENT_TIMESTAMP
     */
    private LocalDateTime messageTime;

    /**
     * 软删除：0 = 未删除，1 = 已删除，非空、默认 0、索引
     */
    private String deleted;

    private String agentData;
}