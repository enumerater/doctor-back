package com.enumerate.disease_detection.POJO.PO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent-Tool关联实体类，对应数据库表 sys_agent_tool_relation
 * 存储Agent可调用的工具列表（多对多关系）
 */
@Data
public class AgentToolRelationPO {
    /**
     * 关联ID，主键、自增、非空
     */
    private Long id;

    /**
     * Agent ID，关联 sys_agent_config.id，非空、外键、索引
     */
    private Long agentId;

    /**
     * 工具 ID，关联 sys_tool_config.id，非空、外键、索引
     */
    private Long toolId;

    /**
     * 创建时间，非空、默认 CURRENT_TIMESTAMP
     */
    private LocalDateTime createTime;

    /**
     * 【业务约束】agentId + toolId 组合为唯一索引，避免同一Agent重复关联同一工具
     */
    private transient String uniqueKeyRemark; // 仅注释用，无数据库映射
}