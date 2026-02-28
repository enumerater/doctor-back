package com.enumerate.disease_detection.MVC.POJO.PO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent对话消息实体类，对应数据库表 sys_agent_chat_message
 * 存储管理端与Agent的对话消息，包含工具调用记录
 */
@Data
@TableName("agri_chat_message")
public class AgentChatMessagePO {
    /**
     * 消息唯一ID，主键、自增、非空
     */
    private Long id;

    /**
     * 所属会话ID，关联 sys_agent_chat_session.id，非空、外键、索引
     */
    private Long sessionId;

    /**
     * 管理员ID，关联 sys_user.id，非空、外键、索引
     */
    private Long adminId;

    /**
     * Agent ID，关联 sys_agent_config.id，非空、外键、索引
     */
    private Long agentId;

    /**
     * 消息角色：0 = 管理员，1 = Agent，非空、索引
     */
    private String messageRole;

    /**
     * 消息内容，非空
     */
    private String messageContent;

    /**
     * 发送时间，非空、默认 CURRENT_TIMESTAMP
     */
    private LocalDateTime messageTime;

    /**
     * 父消息ID（自关联本表id），可空、外键，用于多轮上下文关联
     */
    private Long parentMessageId;

    /**
     * 是否调用工具：0 = 否，1 = 是，非空、默认 0、索引
     */
    private String isCallTool;

    /**
     * 调用的工具ID，关联 sys_tool_config.id，可空、外键、索引
     */
    private Long toolId;

    /**
     * 工具调用参数（JSON格式），可空
     */
    private String toolCallParams;

    /**
     * 工具调用结果，可空
     */
    private String toolCallResult;

    /**
     * 工具调用状态：0 = 调用中，1 = 成功，2 = 失败，可空、索引
     */
    private String toolCallStatus;

    /**
     * 软删除：0 = 未删除，1 = 已删除，非空、默认 0、索引
     */
    private String deleted;
}