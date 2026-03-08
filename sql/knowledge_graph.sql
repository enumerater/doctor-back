-- 知识图谱节点表
CREATE TABLE IF NOT EXISTS `kg_nodes` (
  `id` VARCHAR(64) NOT NULL PRIMARY KEY COMMENT '节点唯一标识',
  `name` VARCHAR(100) NOT NULL COMMENT '节点名称',
  `type` VARCHAR(50) NOT NULL COMMENT '节点类型: crop, pest, pesticide, solar_term, symptom',
  `value` INT DEFAULT 10 COMMENT '节点权重/大小',
  `details` TEXT COMMENT '节点详细描述',
  INDEX `idx_name` (`name`),
  INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识图谱节点表';

-- 知识图谱关系表
CREATE TABLE IF NOT EXISTS `kg_links` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `source_id` VARCHAR(64) NOT NULL COMMENT '源节点ID',
  `target_id` VARCHAR(64) NOT NULL COMMENT '目标节点ID',
  `relation` VARCHAR(100) NOT NULL COMMENT '关系描述',
  INDEX `idx_source` (`source_id`),
  INDEX `idx_target` (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识图谱关系表';
