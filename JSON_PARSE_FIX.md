# JSON解析错误修复说明

## 🐛 问题1：Markdown代码块包裹

在运行v2.0 ReAct循环时遇到JSON解析错误：

```
com.fasterxml.jackson.core.JsonParseException: Unexpected character ('`' (code 96)):
expected a valid value (JSON String, Number, Array, Object or token 'null', 'true' or 'false')
```

**根本原因**：LLM（大语言模型）返回的JSON被markdown代码块包裹了，例如：
```markdown
```json
{
  "taskType": "图像诊断",
  "complexity": "中等"
}
```
```

而Jackson ObjectMapper期望的是纯JSON：
```json
{
  "taskType": "图像诊断",
  "complexity": "中等"
}
```

---

## 🐛 问题2：DTO类型不匹配

遇到第二个错误：

```
com.fasterxml.jackson.databind.exc.MismatchedInputException: Cannot construct instance of `java.util.LinkedHashMap`
(although at least one Creator exists): no String-argument constructor/factory method to deserialize from String value
('预加载番茄灰霉病的典型症状特征知识至多模态识别模块')
```

**根本原因**：DecisionDTO中的`newSteps`字段定义为`List<Map<String, Object>>`，但LLM返回的是字符串数组：

```json
{
  "adjustments": {
    "newSteps": [
      "预加载番茄灰霉病的典型症状特征知识至多模态识别模块",
      "调整RAG检索参数"
    ]
  }
}
```

而DTO期望的是对象数组：
```json
{
  "adjustments": {
    "newSteps": [
      {"step": 1, "action": "预加载知识"}
    ]
  }
}
```

---

## ✅ 修复方案

### 问题1修复：代码层面添加JSON清理函数

在 `ReActLoopService.java` 中添加了 `cleanJsonString()` 方法：

```java
/**
 * 清理JSON字符串，移除markdown代码块标记
 */
private String cleanJsonString(String jsonString) {
    if (jsonString == null || jsonString.isEmpty()) {
        return jsonString;
    }

    String cleaned = jsonString.trim();

    // 移除markdown代码块标记
    // 匹配 ```json...``` 或 ```...```
    if (cleaned.startsWith("```")) {
        int firstNewline = cleaned.indexOf('\n');
        if (firstNewline > 0) {
            cleaned = cleaned.substring(firstNewline + 1);
        } else {
            cleaned = cleaned.replaceFirst("^```(json)?\\s*", "");
        }
    }

    if (cleaned.endsWith("```")) {
        int lastBackticks = cleaned.lastIndexOf("```");
        cleaned = cleaned.substring(0, lastBackticks);
    }

    cleaned = cleaned.trim();
    return cleaned;
}
```

### 2. 在所有JSON解析处使用清理函数

修改了4个关键方法：

#### ✅ executePlanningPhase()
```java
String planJson = plannerAgent.plan(input);
String cleanedJson = cleanJsonString(planJson);  // 清理
ExecutionPlanDTO plan = objectMapper.readValue(cleanedJson, ExecutionPlanDTO.class);
```

#### ✅ executeObservingPhase()
```java
String observationJson = observer.observe(...);
String cleanedJson = cleanJsonString(observationJson);  // 清理
return objectMapper.readValue(cleanedJson, ObservationDTO.class);
```

#### ✅ executeReflectingPhase()
```java
String reflectionJson = reflector.reflect(...);
String cleanedJson = cleanJsonString(reflectionJson);  // 清理
return objectMapper.readValue(cleanedJson, ReflectionDTO.class);
```

#### ✅ executeDecidingPhase()
```java
String decisionJson = decider.decide(...);
String cleanedJson = cleanJsonString(decisionJson);  // 清理
return objectMapper.readValue(cleanedJson, DecisionDTO.class);
```

---

### 3. Prompt层面：强调返回纯JSON

更新了4个Agent的System Message，添加了明确指示：

**PlannerAgent.java**:
```java
"**重要：必须返回纯JSON格式，不要使用markdown代码块（不要```json），直接返回JSON对象**",
"- 直接返回JSON对象，不要包含任何其他文字或格式标记"
```

**ObserverAgent.java**:
```java
"**重要：必须返回纯JSON格式，不要使用markdown代码块（不要```json），直接返回JSON对象**",
"- 直接返回JSON对象，不要包含任何其他文字或格式标记"
```

**ReflectorAgent.java**:
```java
"**重要：必须返回纯JSON格式，不要使用markdown代码块（不要```json），直接返回JSON对象**"
```

**DecisionAgent.java**:
```java
"**重要：必须返回纯JSON格式，不要使用markdown代码块（不要```json），直接返回JSON对象**"
```

---

### 问题2修复：DTO类型宽容化

#### 修改前
```java
// DecisionDTO.PlanAdjustments
private List<Map<String, Object>> newSteps;  // 只能接受对象数组
```

**问题**：LLM可能返回简单的字符串数组：
```json
{
  "newSteps": ["步骤1", "步骤2"]  // 这会导致解析失败
}
```

#### 修改后
```java
// DecisionDTO.PlanAdjustments
private List<Object> newSteps;  // 既能接受字符串数组也能接受对象数组
```

**好处**：现在支持两种格式：
```json
// 格式1：字符串数组（简单）✅
{
  "newSteps": ["步骤1", "步骤2"]
}

// 格式2：对象数组（详细）✅
{
  "newSteps": [
    {"step": 1, "action": "步骤1", "tool": "Tool1"}
  ]
}
```

#### 同时更新Prompt
更新了DecisionAgent的示例格式：
```java
"    \"newSteps\": [\"步骤描述1\", \"步骤描述2\"],"  // 明确示例为字符串数组
```

---

## 🎯 修复效果

### 问题1修复效果

#### 修复前
```
错误：JsonParseException: Unexpected character ('`' (code 96))
原因：LLM返回 ```json{...}```
结果：程序崩溃，无法解析JSON
```

#### 修复后
```
1. cleanJsonString() 自动移除markdown标记
2. prompt明确要求返回纯JSON
3. 日志记录清理前后的JSON（便于调试）
结果：能正常解析JSON，程序稳定运行 ✅
```

---

### 问题2修复效果

#### 修复前
```
错误：MismatchedInputException: Cannot construct instance of LinkedHashMap
原因：newSteps字段要求对象数组，但LLM返回字符串数组
结果：程序崩溃，无法解析DecisionDTO
```

#### 修复后
```
1. 将newSteps类型改为List<Object>（更宽容）
2. 更新prompt示例为字符串数组（引导LLM使用简单格式）
3. 支持字符串数组和对象数组两种格式
结果：能正常解析两种格式，兼容性更强 ✅
```

---

## 📝 测试建议

### 1. 正常情况测试
```bash
curl "http://localhost:8080/agent/agriculture-agent-v2?prompt=小麦叶子发黄&image=&userId=1&sessionId=1"
```

观察日志中的：
- `生成的执行计划（原始）：...`
- `JSON清理前: ...`
- `JSON清理后: ...`

### 2. 异常情况测试

手动构造返回markdown的场景，验证cleanJsonString()能正确处理：
- `\`\`\`json\n{...}\n\`\`\``
- `\`\`\`\n{...}\n\`\`\``
- 纯JSON `{...}`（不需要清理）

---

## 🔧 调试技巧

### 1. 查看原始JSON
日志中会输出：
```
生成的执行计划（原始）：```json
{
  "taskType": "文本咨询",
  ...
}
```
```

### 2. 查看清理后的JSON
日志中会输出：
```
JSON清理后: {
  "taskType": "文本咨询",
  ...
}
```

### 3. 如果仍然报错

检查JSON本身是否有语法错误：
- 缺少逗号
- 引号不匹配
- 字段名错误

可以复制日志中的JSON到 https://jsonlint.com/ 验证。

---

## 🌟 最佳实践

### 1. 双重保障策略
```
提示词约束（第一道防线）
   ↓ （如果LLM仍返回markdown）
代码清理（第二道防线）
   ↓
稳定的JSON解析
```

### 2. 为什么不只用prompt？

**原因**：LLM的输出不完全可控
- 即使prompt明确要求，LLM仍可能用markdown格式
- 不同模型（qwen/gpt/claude）行为不一致
- 温度参数、上下文等会影响输出格式

**解决**：代码层面的防御性编程更可靠

### 3. 为什么不只用代码清理？

**原因**：prompt约束能显著减少问题
- 减少清理逻辑的复杂度
- 提升解析成功率
- 减少不必要的字符串处理开销

**解决**：prompt + 代码双重保障

---

## 🚨 常见问题

### Q1: 仍然报错怎么办？

**检查步骤**：
1. 查看日志中的`JSON清理后`，确认格式正确
2. 复制到jsonlint.com验证语法
3. 检查DTO类字段是否匹配JSON
4. 检查是否有null值但DTO不支持null

### Q2: 性能影响？

**答**：可忽略不计
- cleanJsonString()只做字符串操作
- 时间复杂度：O(n)，n为字符串长度
- 相比LLM调用（秒级），清理耗时可忽略（毫秒级）

### Q3: 能否用正则表达式？

**答**：可以，但当前方案更简单
```java
// 正则方案（更简洁但可读性稍差）
cleaned = cleaned.replaceAll("^```(json)?\\s*|```$", "");

// 当前方案（更清晰，易调试）
if (cleaned.startsWith("```")) {...}
if (cleaned.endsWith("```")) {...}
```

### Q4: MismatchedInputException错误怎么办？

**答**：通常是DTO字段类型与LLM返回的JSON类型不匹配

**常见情况**：
```
错误：Cannot construct instance of LinkedHashMap
原因：字段定义为List<Map>，但LLM返回了List<String>
```

**解决方案**：
1. **方案A（推荐）**：将字段类型改为更宽容的`List<Object>`
   ```java
   private List<Object> newSteps;  // 既能接受字符串也能接受对象
   ```

2. **方案B**：更新Prompt，明确要求返回对象数组
   ```java
   "newSteps": [{"step": 1, "action": "..."}]  // 明确示例
   ```

3. **方案C（兜底）**：在catch块中使用默认值
   ```java
   catch (Exception e) {
       return createDefaultDecision(...);  // 已实现
   }
   ```

### Q5: 如何调试类型不匹配问题？

**步骤**：
1. 查看日志中的原始JSON
2. 对比DTO类的字段定义
3. 找出不匹配的字段
4. 选择修改DTO或修改Prompt

**示例**：
```
// 日志显示
"newSteps": ["步骤1", "步骤2"]  // 字符串数组

// DTO定义
private List<Map<String, Object>> newSteps;  // 期望对象数组

// 解决：改为
private List<Object> newSteps;  // 支持两种格式 ✅
```

---

## 📚 相关资源

- **Jackson JSON解析**：https://github.com/FasterXML/jackson
- **LLM输出格式控制**：https://platform.openai.com/docs/guides/structured-outputs
- **JSON格式验证**：https://jsonlint.com/

---

## 📋 修复总结

| 问题 | 原因 | 解决方案 | 文件 |
|------|------|---------|------|
| **JsonParseException** | LLM返回markdown包裹的JSON | 添加cleanJsonString()清理函数 | ReActLoopService.java |
| **MismatchedInputException** | DTO字段类型与JSON不匹配 | 将newSteps改为List&lt;Object&gt; | DecisionDTO.java |
| **提升成功率** | LLM输出不稳定 | 4个Agent的prompt强化约束 | 4个Agent接口 |

### 修改的文件清单

1. ✅ **ReActLoopService.java** - 添加cleanJsonString()，更新4个解析方法
2. ✅ **DecisionDTO.java** - 修改newSteps类型为List&lt;Object&gt;
3. ✅ **PlannerAgent.java** - 强化prompt约束
4. ✅ **ObserverAgent.java** - 强化prompt约束
5. ✅ **ReflectorAgent.java** - 强化prompt约束
6. ✅ **DecisionAgent.java** - 强化prompt约束 + 更新示例
7. ✅ **JSON_PARSE_FIX.md** - 本文档

---

**双重保障策略生效！现在系统能稳定处理各种LLM输出格式了。** ✅🎉

