package com.enumerate.disease_detection.MVC.POJO.PO;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 农场实体类
 * 修复：
 * 1. 添加无参构造器（MyBatis 必须）
 * 2. 调整字段类型与数据库实际存储类型匹配
 * 3. 统一时间类型为 LocalDateTime（更推荐的 Java 8 时间类型）
 */
@Data
@TableName("farm")
@Builder
@NoArgsConstructor  // 必须添加无参构造器
@AllArgsConstructor // 显式声明全参构造器
public class FarmPO {
    private Long id;
    private Long userId;
    private String name;
    private String location;

    private String area;

    // 修复2：plotCount 是数量，改为 Integer 类型
    private Integer plotCount;

    @TableField(fill = FieldFill.INSERT)
    // 修复3：推荐使用 LocalDateTime 替代 DateTime（兼容 MyBatis 自动映射）
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
