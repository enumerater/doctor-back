package com.enumerate.disease_detection.POJO.PO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent对话会话实体类，对应数据库表 sys_agent_chat_session
 * 存储管理端与Agent的对话会话记录
 */
@Data
public class AgentChatSessionPO {
    /**
     * Agent会话唯一ID，主键、自增、非空
     */
    private Long id;

    /**
     * 管理员ID，关联 sys_user.id，非空、外键、索引
     */
    private Long adminId;

    /**
     * Agent ID，关联 sys_agent_config.id，非空、外键、索引
     */
    private Long agentId;

    /**
     * 会话标题，可空
     */
    private String sessionTitle;

    /**
     * 会话状态：0 = 结束，1 = 进行中，非空、默认 1、索引
     */
    private String sessionStatus;

    /**
     * 创建时间，非空、默认 CURRENT_TIMESTAMP
     */
    private LocalDateTime createTime;

    /**
     * 更新时间，非空、ON UPDATE CURRENT_TIMESTAMP
     */
    private LocalDateTime updateTime;

    /**
     * 最后对话时间，可空、索引
     */
    private LocalDateTime lastChatTime;

    /**
     * 软删除：0 = 未删除，1 = 已删除，非空、默认 0、索引
     */
    private String deleted;
}