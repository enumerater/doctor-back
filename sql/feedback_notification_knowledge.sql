-- 反馈表
CREATE TABLE IF NOT EXISTS `feedback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '提交反馈的用户ID',
  `diagnosis_id` VARCHAR(64) NOT NULL COMMENT '关联的诊断ID',
  `accuracy` VARCHAR(20) NOT NULL COMMENT 'correct/partial/incorrect',
  `correct_disease` VARCHAR(100) DEFAULT '' COMMENT '正确病害名称（accuracy非correct时填写）',
  `rating` INT NOT NULL COMMENT '评分1-5',
  `comment` TEXT COMMENT '评论',
  `crop_type` VARCHAR(50) DEFAULT NULL COMMENT '作物类型',
  `diagnosed_disease` VARCHAR(100) DEFAULT NULL COMMENT '诊断出的病害',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/rejected',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_diagnosis_id` (`diagnosis_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='诊断反馈表';

-- 通知表
CREATE TABLE IF NOT EXISTS `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '接收通知的用户ID',
  `type` VARCHAR(30) NOT NULL COMMENT 'disease_alert/treatment_remind/safety_interval/weather_alert/system',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` TEXT COMMENT '通知内容',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0=未读, 1=已读',
  `link` VARCHAR(500) DEFAULT NULL COMMENT '跳转链接',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 知识库表
CREATE TABLE IF NOT EXISTS `knowledge` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL COMMENT '病害名称',
  `crop` VARCHAR(50) NOT NULL COMMENT '作物类型',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '病害分类（真菌/细菌/病毒等）',
  `symptoms` TEXT COMMENT '症状描述',
  `treatment` TEXT COMMENT '防治方法',
  `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT 'draft/published',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_crop` (`crop`),
  INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';
