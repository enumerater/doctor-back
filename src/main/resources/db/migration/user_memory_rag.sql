# -- 用户长期记忆 RAG 系统 - 数据库迁移脚本
# -- 执行前请备份数据库
#
# -- 1. 改造 vector_store 表（新增字段）
# ALTER TABLE vector_store
#   ADD COLUMN user_id BIGINT NOT NULL DEFAULT 0 COMMENT '所属用户ID' AFTER id,
#   ADD COLUMN memory_type VARCHAR(50) DEFAULT 'conversation_extract' COMMENT '记忆来源类型',
#   ADD COLUMN source_session_id VARCHAR(100) DEFAULT NULL COMMENT '来源会话ID',
#   ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
#   ADD INDEX idx_user_id (user_id);
#
# ALTER TABLE vector_store MODIFY COLUMN document_path VARCHAR(255) DEFAULT NULL;
#
# -- 清除旧的病害知识数据
# DELETE FROM vector_store WHERE user_id = 0;

-- 2. 新建 memory_process_log 表（追踪已处理的会话）
CREATE TABLE memory_process_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  session_id VARCHAR(100) NOT NULL,
  processed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  memory_count INT DEFAULT 0,
  status VARCHAR(20) DEFAULT 'SUCCESS',
  error_message TEXT DEFAULT NULL,
  UNIQUE INDEX uk_session_id (session_id),
  INDEX idx_user_id (user_id)
);
