package com.enumerate.disease_detection.MVC.POJO.PO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 对话会话实体类，对应数据库表 agri_chat_session
 * 记录用户与农业大模型的单次对话会话（一个会话对应一组多轮对话）
 */
@Data
@TableName("chat_session")
@Builder
public class ChatSessionPO {
    /**
     * 会话唯一ID，主键、自增、非空
     */
    private Long id;

    /**
     * 所属用户ID，关联 sys_user.id，非空、外键、索引
     */
    private Long userId;

    /**
     * 会话标题（自动提取首条消息摘要），可空
     */
    private String sessionTitle;

    /**
     * 会话状态：0 = 结束，1 = 进行中，非空、默认 1、索引
     */
    private String sessionStatus;

    /**
     * 创建时间，非空、默认 CURRENT_TIMESTAMP
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间，非空、ON UPDATE CURRENT_TIMESTAMP
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 最后对话时间，可空、索引
     */
    private LocalDateTime lastChatTime;

    /**
     * 软删除：0 = 未删除，1 = 已删除，非空、默认 0、索引
     */
    private String deleted;

    private String sessionId;

    private String sessionType;
}