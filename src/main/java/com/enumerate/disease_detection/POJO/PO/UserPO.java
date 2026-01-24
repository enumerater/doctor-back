package com.enumerate.disease_detection.POJO.PO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类，对应数据库用户表
 * @author （可补充作者信息）
 * @date （可补充创建日期）
 */
@Data
@TableName("sys_user")
@Builder
public class UserPO {
    /**
     * 用户唯一ID，主键、自增、非空
     */
    private Long id;

    /**
     * 用户名（登录账号），非空、唯一索引
     */
    private String username;

    /**
     * 密码（建议MD5/SHA256加密存储），非空
     */
    private String password;

    /**
     * 手机号（用于注册/登录验证），唯一索引、可空
     */
    private String phone;

    /**
     * 邮箱，唯一索引、可空
     */
    private String email;

    /**
     * 角色：0 = 普通用户（用户端），1 = 管理员（管理端），非空、索引
     */
    private String role;

    /**
     * 状态：0 = 禁用，1 = 正常，非空
     */
    private String status;

    /**
     * 头像URL，可空
     */
    private String avatar;

    /**
     * 创建时间，非空、默认CURRENT_TIMESTAMP
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间，非空、ON UPDATE CURRENT_TIMESTAMP
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /**
     * 最后登录时间，可空
     */
    private LocalDateTime lastLoginTime;

    /**
     * 软删除：0 = 未删除，1 = 已删除，非空、默认0、索引
     */
    private String deleted;

    private Long sessionId;
}