-- 施药记录表
CREATE TABLE IF NOT EXISTS pesticide_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plot_id BIGINT NOT NULL COMMENT '关联地块ID',
    medicine_name VARCHAR(100) NOT NULL COMMENT '药剂名称',
    category VARCHAR(50) NOT NULL COMMENT '分类：杀虫剂, 杀菌剂, 除草剂, 肥料',
    dosage VARCHAR(50) COMMENT '用量',
    unit VARCHAR(20) COMMENT '单位',
    purpose VARCHAR(200) COMMENT '施用目的',
    application_date DATE NOT NULL COMMENT '施用日期',
    effect_evaluation INT DEFAULT 0 COMMENT '效果评价 1-5',
    effect_remarks TEXT COMMENT '效果评价备注',
    status VARCHAR(20) DEFAULT 'COMPLETED' COMMENT '状态: PENDING, COMPLETED',
    remarks TEXT COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_plot_id (plot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='施药记录表';

-- 田间随笔表
CREATE TABLE IF NOT EXISTS field_note (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plot_id BIGINT NOT NULL COMMENT '关联地块ID',
    content TEXT NOT NULL COMMENT '随笔内容',
    images TEXT COMMENT '图片URL列表，JSON存储',
    date DATETIME NOT NULL COMMENT '记录日期',
    is_ai_generated TINYINT(1) DEFAULT 0 COMMENT '是否AI生成',
    weather_info VARCHAR(100) COMMENT '天气信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_plot_id (plot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='田间随笔表';
