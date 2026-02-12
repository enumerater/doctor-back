# Skills系统使用文档

## 一、系统概述

Skills系统是一个可扩展的外部工具调用框架，允许大模型在ReAct循环中动态调用各种专业技能（如病害识别、价格查询、天气预报等），提升Agent的实际问题解决能力。

## 二、系统架构

### 核心组件

1. **SkillDefinitionDTO** - Skill定义数据传输对象
2. **DynamicSkillTool** - 动态Skill工具类，根据配置调用API
3. **SkillApiClient** - 统一的API调用客户端
4. **SkillLoaderService** - Skill动态加载服务
5. **SkillAgent** - LLM智能Skill调度Agent
6. **ReActLoopService** - 集成了Skill调用的ReAct循环服务

### 工作流程

```
用户输入
  ↓
加载用户的Skills配置（根据agent_configs.enabled_skill_ids）
  ↓
ReAct循环执行阶段
  ↓
SkillAgent分析是否需要调用Skill
  ↓
如果需要 → 调用Python FastAPI → 获取结果 → 融入工作记忆
  ↓
专家分析 → 生成最终结果
```

## 三、数据库配置

### 1. skills表结构示例

```sql
CREATE TABLE skills (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT 'Skill名称',
  description TEXT COMMENT 'Skill描述',
  icon VARCHAR(255) COMMENT '图标URL',
  category VARCHAR(50) COMMENT '分类',
  enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  triggers JSON COMMENT '触发关键词',
  params JSON COMMENT 'API配置参数'
);
```

### 2. 插入示例数据

#### （1）病害图片识别
```sql
INSERT INTO skills (name, description, icon, category, enabled, triggers, params) VALUES
(
  '病害图片识别',
  '通过AI视觉模型识别植物病害图片，诊断病害类型和严重程度',
  'disease-icon.png',
  'vision',
  1,
  '["病害", "识别", "诊断", "图片", "叶片"]',
  '{
    "endpoint": "/api/disease-recognition",
    "method": "GET",
    "timeout": 30,
    "params": [
      {
        "name": "image_url",
        "type": "string",
        "description": "病害图片的URL",
        "required": true
      },
      {
        "name": "crop_type",
        "type": "string",
        "description": "作物类型",
        "required": false,
        "defaultValue": "unknown"
      }
    ]
  }'
);
```

#### （2）施肥计算器
```sql
INSERT INTO skills (name, description, icon, category, enabled, triggers, params) VALUES
(
  '施肥计算器',
  '根据作物类型、生长阶段、土壤条件等因素，智能计算施肥方案',
  'fertilizer-icon.png',
  'calculation',
  1,
  '["施肥", "肥料", "用量", "计算", "配方"]',
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
        "description": "生长阶段",
        "required": true
      },
      {
        "name": "area",
        "type": "number",
        "description": "种植面积（亩）",
        "required": true
      },
      {
        "name": "soil_type",
        "type": "string",
        "description": "土壤类型",
        "required": false
      }
    ]
  }'
);
```

#### （3）价格查询
```sql
INSERT INTO skills (name, description, icon, category, enabled, triggers, params) VALUES
(
  '价格查询',
  '查询农产品或农资的实时市场价格',
  'price-icon.png',
  'query',
  1,
  '["价格", "多少钱", "市场", "行情", "报价"]',
  '{
    "endpoint": "/api/price-query",
    "method": "GET",
    "timeout": 10,
    "params": [
      {
        "name": "product_name",
        "type": "string",
        "description": "产品名称",
        "required": true
      },
      {
        "name": "region",
        "type": "string",
        "description": "地区",
        "required": false
      }
    ]
  }'
);
```

#### （4）天气预报
```sql
INSERT INTO skills (name, description, icon, category, enabled, triggers, params) VALUES
(
  '天气预报',
  '查询指定地区的天气预报，包括温度、降雨、风力等信息',
  'weather-icon.png',
  'weather',
  1,
  '["天气", "预报", "气温", "下雨", "气象"]',
  '{
    "endpoint": "/api/weather-forecast",
    "method": "GET",
    "timeout": 10,
    "params": [
      {
        "name": "location",
        "type": "string",
        "description": "地区名称（省市或经纬度）",
        "required": true
      },
      {
        "name": "days",
        "type": "number",
        "description": "预报天数（1-7）",
        "required": false,
        "defaultValue": "3"
      }
    ]
  }'
);
```

### 3. agent_configs表配置

在`agent_configs`表的`enabled_skill_ids`字段中配置启用的Skill ID列表：

```sql
-- 方式1：逗号分隔的字符串
UPDATE agent_configs SET enabled_skill_ids = '1,2,3,4' WHERE id = 1;

-- 方式2：JSON数组（推荐）
UPDATE agent_configs SET enabled_skill_ids = '[1,2,3,4]' WHERE id = 1;
```

## 四、Python FastAPI接口规范

你需要在Python FastAPI中实现以下接口：

### 1. 病害图片识别接口

```python
@app.get("/api/disease-recognition")
async def disease_recognition(image_url: str, crop_type: str = "unknown"):
    """
    病害识别接口

    参数:
        image_url: 图片URL
        crop_type: 作物类型

    返回:
        {
            "result": "识别结果描述",
            "disease_type": "病害类型",
            "severity": "严重程度",
            "confidence": 0.95
        }
    """
    # 你的实现代码
    pass
```

### 2. 施肥计算器接口

```python
@app.post("/api/fertilizer-calculator")
async def fertilizer_calculator(data: dict):
    """
    施肥计算器接口

    请求体:
        {
            "crop_type": "水稻",
            "growth_stage": "分蘖期",
            "area": 10,
            "soil_type": "水田"
        }

    返回:
        {
            "result": "施肥方案描述",
            "nitrogen": 15.5,
            "phosphorus": 8.0,
            "potassium": 12.0,
            "unit": "kg/亩"
        }
    """
    crop_type = data.get("crop_type")
    growth_stage = data.get("growth_stage")
    area = data.get("area")
    soil_type = data.get("soil_type")

    # 你的计算逻辑
    pass
```

### 3. 价格查询接口

```python
@app.get("/api/price-query")
async def price_query(product_name: str, region: str = None):
    """
    价格查询接口

    参数:
        product_name: 产品名称
        region: 地区（可选）

    返回:
        {
            "result": "价格信息描述",
            "product": "尿素",
            "price": 2850,
            "unit": "元/吨",
            "region": "河南",
            "date": "2024-01-15"
        }
    """
    # 你的实现代码
    pass
```

### 4. 天气预报接口

```python
@app.get("/api/weather-forecast")
async def weather_forecast(location: str, days: int = 3):
    """
    天气预报接口

    参数:
        location: 地区名称
        days: 预报天数

    返回:
        {
            "result": "天气预报描述",
            "location": "北京",
            "forecast": [
                {
                    "date": "2024-01-15",
                    "weather": "晴",
                    "temp_high": 8,
                    "temp_low": -3,
                    "wind": "北风3-4级"
                }
            ]
        }
    """
    # 你的实现代码
    pass
```

## 五、如何调用ReAct服务

修改你的Controller，传入userId和agentConfigId：

```java
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Autowired
    private ReActLoopService reActLoopService;

    @GetMapping("/chat")
    public SseEmitter chat(
        @RequestParam String input,
        @RequestParam Long userId,
        @RequestParam(required = false) Long agentConfigId
    ) {
        SseEmitter emitter = new SseEmitter(300000L);
        reActLoopService.executeReActLoop(emitter, input, userId, agentConfigId);
        return emitter;
    }
}
```

## 六、扩展新的Skill

### 步骤1：在数据库添加Skill配置

```sql
INSERT INTO skills (name, description, icon, category, enabled, triggers, params) VALUES
(
  '新Skill名称',
  'Skill功能描述',
  'icon.png',
  'category',
  1,
  '["关键词1", "关键词2"]',
  '{
    "endpoint": "/api/your-endpoint",
    "method": "GET/POST",
    "timeout": 30,
    "params": [...]
  }'
);
```

### 步骤2：在Python FastAPI实现对应接口

```python
@app.get("/api/your-endpoint")
async def your_skill_function(param1: str, param2: int):
    # 实现你的业务逻辑
    return {
        "result": "返回结果"
    }
```

### 步骤3：在agent_configs中启用该Skill

```sql
UPDATE agent_configs
SET enabled_skill_ids = '[1,2,3,4,5]'  -- 添加新Skill的ID
WHERE id = 你的配置ID;
```

## 七、注意事项

1. **API响应格式**：所有Python接口必须返回JSON，且包含`result`字段
2. **超时设置**：合理设置timeout，避免长时间等待
3. **错误处理**：Python接口应返回明确的错误信息
4. **参数验证**：前端和后端都要做参数校验
5. **安全性**：敏感操作需要权限验证
6. **性能优化**：对于高频调用的Skill，考虑缓存策略

## 八、测试示例

### 测试病害识别
```
用户输入：请帮我看看这张图片的作物得了什么病 http://example.com/leaf.jpg
系统：
  1. 加载Skills（病害图片识别等）
  2. SkillAgent判断需要调用"病害图片识别"
  3. 调用Python接口识别
  4. 返回诊断结果
```

### 测试价格查询
```
用户输入：尿素现在多少钱一吨？
系统：
  1. 加载Skills（价格查询等）
  2. SkillAgent判断需要调用"价格查询"
  3. 调用Python接口查询
  4. 返回价格信息
```

## 九、常见问题

### Q1: Skill没有被调用？
- 检查`enabled_skill_ids`配置是否正确
- 检查Skill的`enabled`字段是否为true
- 查看日志，确认SkillAgent的分析结果

### Q2: API调用失败？
- 确认Python FastAPI服务已启动
- 检查`FAST_API_BASE_URL`配置（默认http://127.0.0.1:8000）
- 查看Python接口的错误日志

### Q3: 如何调试Skill调用？
- 查看日志中的"Skill调用计划"和"Skill执行成功/失败"信息
- 在Python接口中添加日志输出
- 使用Postman测试Python接口

---

**技术支持**: 如有问题，请查看日志文件或联系开发团队
