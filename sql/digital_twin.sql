-- 更新地块表以支持数字孪生
ALTER TABLE plot ADD COLUMN status VARCHAR(20) DEFAULT 'normal' COMMENT '状态: healthy, warning, danger, normal';
ALTER TABLE plot ADD COLUMN grid_x INT DEFAULT 0 COMMENT '3D网格X坐标';
ALTER TABLE plot ADD COLUMN grid_y INT DEFAULT 0 COMMENT '3D网格Y坐标';
ALTER TABLE plot ADD COLUMN health_score INT DEFAULT 100 COMMENT '健康评分 0-100';

-- 传感器数据表 (实时与历史)
CREATE TABLE IF NOT EXISTS sensor_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plot_id BIGINT NOT NULL COMMENT '关联地块ID',
    temperature FLOAT COMMENT '环境温度 (℃)',
    humidity FLOAT COMMENT '土壤湿度 (%)',
    npk_n FLOAT COMMENT '氮含量',
    npk_p FLOAT COMMENT '磷含量',
    npk_k FLOAT COMMENT '钾含量',
    light_intensity FLOAT COMMENT '光照强度 (lux)',
    soil_moisture FLOAT COMMENT '土壤水分 (%)',
    is_irrigating TINYINT(1) DEFAULT 0 COMMENT '是否正在灌溉',
    description TEXT COMMENT '状态描述',
    recorded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_plot_id (plot_id),
    INDEX idx_recorded_at (recorded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='传感器监测数据表';

-- 模拟初始数据 (可选)
-- UPDATE plot SET grid_x = 0, grid_y = 0 WHERE id = 1;
-- UPDATE plot SET grid_x = 1, grid_y = 0 WHERE id = 2;
