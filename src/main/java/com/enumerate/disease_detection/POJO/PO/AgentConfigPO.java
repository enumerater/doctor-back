package com.enumerate.disease_detection.POJO.PO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent配置实体类，对应数据库表 sys_agent_config
 * 管理端专用，存储Agent的基础配置信息
 */
@Data
public class AgentConfigPO {
    /**
     * Agent唯一ID，主键、自增、非空
     */
    private Long id;

    /**
     * Agent名称（如 “农业数据管理 Agent”），非空
     */
    private String agentName;

    /**
     * Agent编码（如 agri-data-agent），非空、唯一索引
     */
    private String agentCode;

    /**
     * Agent描述（功能、适用场景），可空
     */
    private String description;

    /**
     * 负责人（管理员）ID，关联 sys_user.id，非空、外键、索引
     */
    private Long responsibleAdminId;

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

    /**
     * 软删除：0 = 未删除，1 = 已删除，非空、默认 0、索引
     */
    private String deleted;
}