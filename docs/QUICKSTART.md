# Skills系统快速开始

## 快速集成步骤

### 1️⃣ 数据库初始化

执行初始化脚本：
```bash
mysql -u your_user -p your_database < docs/skills_init.sql
```

或手动在数据库中执行 `docs/skills_init.sql` 的内容。

### 2️⃣ 启用Skills

为你的Agent配置启用Skills：

```sql
-- 启用所有4个默认Skills
UPDATE agent_configs
SET enabled_skill_ids = '[1,2,3,4]'
WHERE id = 你的Agent配置ID;
```

### 3️⃣ 实现Python接口

在你的Python FastAPI项目中实现以下4个接口：

#### （1）病害图片识别 `/api/disease-recognition`
```python
@app.get("/api/disease-recognition")
async def disease_recognition(image_url: str, crop_type: str = "unknown"):
    # TODO: 实现病害识别逻辑
    return {
        "result": "诊断结果文本描述",
        "disease_type": "白粉病",
        "severity": "中度",
        "confidence": 0.92
    }
```

#### （2）施肥计算器 `/api/fertilizer-calculator`
```python
@app.post("/api/fertilizer-calculator")
async def fertilizer_calculator(data: dict):
    # TODO: 实现施肥计算逻辑
    return {
        "result": "施肥方案文本描述",
        "nitrogen": 15.5,   # 氮肥 kg/亩
        "phosphorus": 8.0,  # 磷肥 kg/亩
        "potassium": 12.0   # 钾肥 kg/亩
    }
```

#### （3）价格查询 `/api/price-query`
```python
@app.get("/api/price-query")
async def price_query(product_name: str, region: str = "全国"):
    # TODO: 实现价格查询逻辑
    return {
        "result": "尿素当前市场价格为2850元/吨",
        "product": product_name,
        "price": 2850,
        "unit": "元/吨",
        "region": region
    }
```

#### （4）天气预报 `/api/weather-forecast`
```python
@app.get("/api/weather-forecast")
async def weather_forecast(location: str, days: int = 3):
    # TODO: 实现天气查询逻辑
    return {
        "result": "未来3天天气：晴转多云，最高温8℃",
        "location": location,
        "forecast": [
            {
                "date": "2024-01-15",
                "weather": "晴",
                "temp_high": 8,
                "temp_low": -3
            }
        ]
    }
```

### 4️⃣ 配置FastAPI地址

如果你的Python服务不在 `http://127.0.0.1:8000`，修改：

```java
// SkillApiClient.java
private static final String FAST_API_BASE_URL = "http://你的地址:端口";
```

### 5️⃣ 修改Controller调用方式

在你的AgentController中，传入userId和agentConfigId：

```java
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
```

### 6️⃣ 测试

启动Java服务和Python服务后，发送请求：

```bash
# 测试病害识别
curl "http://localhost:8080/api/agent/chat?userId=1&input=请帮我看看这张图片http://example.com/leaf.jpg"

# 测试价格查询
curl "http://localhost:8080/api/agent/chat?userId=1&input=尿素现在多少钱一吨"

# 测试天气查询
curl "http://localhost:8080/api/agent/chat?userId=1&input=北京明天天气怎么样"
```

---

## 需要的Python接口总结

| Skill | 接口路径 | 方法 | 必需参数 | 返回字段 |
|-------|---------|------|---------|---------|
| 病害图片识别 | `/api/disease-recognition` | GET | image_url | result |
| 施肥计算器 | `/api/fertilizer-calculator` | POST | crop_type, growth_stage, area | result |
| 价格查询 | `/api/price-query` | GET | product_name | result |
| 天气预报 | `/api/weather-forecast` | GET | location | result |

**关键**：所有接口必须返回包含 `result` 字段的JSON对象。

---

## 添加新Skill

### 步骤1：数据库插入
```sql
INSERT INTO skills (name, description, category, enabled, triggers, params) VALUES
('你的Skill名称', '功能描述', 'category', 1, '["关键词"]', '{"endpoint":"/api/your-api","method":"GET",...}');
```

### 步骤2：实现Python接口
```python
@app.get("/api/your-api")
async def your_skill(param1: str):
    return {"result": "你的结果"}
```

### 步骤3：启用Skill
```sql
UPDATE agent_configs SET enabled_skill_ids = '[1,2,3,4,5]' WHERE id = 你的配置ID;
```

---

## 故障排查

### Skills没有被调用？
1. 检查数据库 `agent_configs.enabled_skill_ids` 是否配置正确
2. 检查 `skills.enabled` 是否为 1
3. 查看日志中的 "Skill调用计划" 信息

### API调用失败？
1. 确认Python FastAPI服务已启动
2. 检查 `FAST_API_BASE_URL` 配置是否正确
3. 用Postman测试Python接口是否正常

### LLM没有识别到应该用Skill？
1. 优化 `triggers` 关键词列表
2. 改进 `description` 描述更加明确
3. 用户输入更明确（如："查询尿素价格" 而不是 "尿素"）

---

**完整文档**: 查看 `docs/SKILLS_SYSTEM_GUIDE.md`
