package com.enumerate.disease_detection.POJO.PO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 工具配置实体类，对应数据库表 sys_tool_config
 * 管理端专用，存储Agent可调用的工具列表
 */
@Data
public class ToolConfigPO {
    /**
     * 工具唯一ID，主键、自增、非空
     */
    private Long id;

    /**
     * 工具名称（如 “病虫害数据查询”、“天气查询”），非空
     */
    private String toolName;

    /**
     * 工具编码（如 get-disease-data），非空、唯一索引
     */
    private String toolCode;

    /**
     * 工具类型（如数据查询、指令执行、文件操作），可空、索引
     */
    private String toolType;

    /**
     * 工具调用的API地址，非空
     */
    private String apiUrl;

    /**
     * 请求方法（GET/POST/PUT/DELETE），非空、默认 POST
     */
    private String requestMethod;

    /**
     * 请求参数模板（JSON格式，如 {"crop":"","region":""}），可空
     */
    private String requestParams;

    /**
     * 工具描述（功能、参数说明），可空
     */
    private String description;

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
}