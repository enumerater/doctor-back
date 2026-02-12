-- Skills系统数据库初始化脚本

-- ============================================
-- 1. 创建skills表（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS skills (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Skill ID',
  name VARCHAR(100) NOT NULL COMMENT 'Skill名称',
  description TEXT COMMENT 'Skill描述（提供给LLM理解）',
  icon VARCHAR(255) COMMENT '图标URL',
  category VARCHAR(50) COMMENT '分类（vision/calculation/query/weather等）',
  enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用（0=禁用，1=启用）',
  triggers JSON COMMENT '触发关键词列表（JSON数组）',
  params JSON COMMENT 'API配置参数（JSON对象）',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Skills技能配置表';

-- ============================================
-- 2. 插入默认Skills
-- ============================================

-- Skill 1: 病害图片识别
INSERT INTO skills (name, description, icon, category, enabled, triggers, params) VALUES
(
  '病害图片识别',
  '通过AI视觉模型识别植物病害图片，诊断病害类型和严重程度。适用于各类作物的叶片、茎秆、果实病害识别。',
  'disease-icon.png',
  'vision',
  1,
  '["病害", "识别", "诊断", "图片", "叶片", "看图", "分析"]',
  '{
    "endpoint": "/api/disease-recognition",
    "method": "GET",
    "timeout": 30,
    "params": [
      {
        "name": "image_url",
        "type": "string",
        "description": "病害图片的URL地址",
        "required": true
      },
      {
        "name": "crop_type",
        "type": "string",
        "description": "作物类型（水稻、小麦、玉米等）",
        "required": false,
        "defaultValue": "unknown"
      }
    ]
  }'
);

-- Skill 2: 施肥计算器
INSERT INTO skills (name, description, icon, category, enabled, triggers, params) VALUES
(
  '施肥计算器',
  '根据作物类型、生长阶段、土壤条件和种植面积，智能计算科学的施肥方案（氮磷钾配比和用量）。',
  'fertilizer-icon.png',
  'calculation',
  1,
  '["施肥", "肥料", "用量", "计算", "配方", "多少肥", "怎么施肥"]',
  '{
    "endpoint": "/api/fertilizer-calculator",
    "method": "POST",
    "timeout": 15,
    "params": [
      {
        "name": "crop_type",
        "type": "string",
        "description": "作物类型",
        "required": true
      },
      {
        "name": "growth_stage",
        "type": "string",
        "description": "生长阶段（苗期/分蘖期/抽穗期等）",
        "required": true
      },
      {
        "name": "area",
        "type": "number",
        "description": "种植面积（单位：亩）",
        "required": true
      },
      {
        "name": "soil_type",
        "type": "string",
        "description": "土壤类型（水田/旱地/沙土等）",
        "required": false,
        "defaultValue": "普通土壤"
      }
    ]
  }'
);

-- Skill 3: 价格查询
INSERT INTO skills (name, description, icon, category, enabled, triggers, params) VALUES
(
  '价格查询',
  '查询农产品（粮食、蔬菜、水果）或农资（化肥、农药、种子）的实时市场价格和行情走势。',
  'price-icon.png',
  'query',
  1,
  '["价格", "多少钱", "市场", "行情", "报价", "成本"]',
  '{
    "endpoint": "/api/price-query",
    "method": "GET",
    "timeout": 10,
    "params": [
      {
        "name": "product_name",
        "type": "string",
        "description": "产品名称（如：尿素、水稻、西红柿）",
        "required": true
      },
      {
        "name": "region",
        "type": "string",
        "description": "地区（省市名称，如：河南、山东）",
        "required": false,
        "defaultValue": "全国"
      }
    ]
  }'
);

-- Skill 4: 天气预报
INSERT INTO skills (name, description, icon, category, enabled, triggers, params) VALUES
(
  '天气预报',
  '查询指定地区的天气预报，包括温度、降雨概率、风力、湿度等气象信息，支持1-7天预报。',
  'weather-icon.png',
  'weather',
  1,
  '["天气", "预报", "气温", "下雨", "气象", "明天天气", "未来天气"]',
  '{
    "endpoint": "/api/weather-forecast",
    "method": "GET",
    "timeout": 10,
    "params": [
      {
        "name": "location",
        "type": "string",
        "description": "地区名称（省市或县区，如：北京、石家庄）",
        "required": true
      },
      {
        "name": "days",
        "type": "number",
        "description": "预报天数（1-7天）",
        "required": false,
        "defaultValue": "3"
      }
    ]
  }'
);

-- ============================================
-- 3. 修改agent_configs表（如果enabled_skill_ids字段不存在）
-- ============================================
-- 注意：如果该字段已存在，跳过此步骤

-- ALTER TABLE agent_configs ADD COLUMN enabled_skill_ids VARCHAR(500) DEFAULT NULL COMMENT '启用的Skill ID列表（逗号分隔或JSON数组）';

-- ============================================
-- 4. 为现有的Agent配置启用所有Skills（示例）
-- ============================================
-- 将所有4个默认Skill启用到ID为1的Agent配置
-- UPDATE agent_configs SET enabled_skill_ids = '[1,2,3,4]' WHERE id = 1;

-- 或者为所有Agent配置启用（谨慎使用）
-- UPDATE agent_configs SET enabled_skill_ids = '[1,2,3,4]' WHERE enabled_skill_ids IS NULL;

-- ============================================
-- 5. 验证数据
-- ============================================
-- 查看所有Skills
SELECT id, name, category, enabled FROM skills;

-- 查看Agent配置的Skills
SELECT id, name, enabled_skill_ids FROM agent_configs;

-- ============================================
-- 说明
-- ============================================
-- 1. enabled_skill_ids支持两种格式：
--    - 逗号分隔：'1,2,3,4'
--    - JSON数组：'[1,2,3,4]'（推荐）
--
-- 2. 如需禁用某个Skill，将skills表的enabled改为0
--
-- 3. 如需为不同用户启用不同的Skills，修改对应的agent_configs记录
--
-- 4. 新增Skill时，只需INSERT到skills表，然后更新agent_configs即可
