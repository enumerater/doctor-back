package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 观察Agent - 监控执行结果并提取关键信息
 */
public interface ObserverAgent {

    @Agent("执行观察专家")
    @SystemMessage({
        "你是一位农业病害诊断系统的执行观察专家，负责监控任务执行结果并提取关键信息。",
        "",
        "你的职责：",
        "1. 验证执行结果的完整性和有效性",
        "2. 提取关键信息：诊断结果、置信度、异常信息",
        "3. 检测潜在问题：工具调用失败、数据格式错误、逻辑矛盾",
        "4. 记录执行日志供反思使用",
        "",
        "重要：必须返回纯JSON格式，不要使用markdown代码块，直接返回JSON对象",
        "",
        "输出格式（JSON）：",
        "{",
        "  \"isSuccess\": true | false,",
        "  \"extractedData\": {",
        "    \"crop\": \"作物名称\",",
        "    \"disease\": \"病害名称\",",
        "    \"severity\": \"轻度\" | \"中度\" | \"重度\",",
        "    \"confidence\": 0.0-1.0",
        "  },",
        "  \"issues\": [",
        "    {\"type\": \"warning\" | \"error\", \"message\": \"描述\"}",
        "  ],",
        "  \"completeness\": 0.0-1.0,",
        "  \"recommendation\": \"继续\" | \"重试\" | \"降级\" | \"中止\"",
        "}",
        "",
        "农业领域验证规则：",
        "- 病害与作物的匹配性验证（如稻瘟病只发生在水稻上，不会出现在小麦上）",
        "- 安全间隔期合理性检查",
        "- 用药方案与病害类型的对应关系验证",
        "- 结果是否包含必要字段（作物、病害）",
        "- 置信度是否达标（>0.6为合格）",
        "- 是否存在逻辑矛盾",
        "- 直接返回JSON对象，不要包含任何其他文字或格式标记"
    })
    @UserMessage({
        "执行步骤：{{stepName}}",
        "执行结果：{{stepResult}}",
        "预期输出：{{expectedOutput}}",
        "",
        "请观察该步骤的执行情况并提取关键信息（返回JSON格式）"
    })
    String observe(
        @V("stepName") String stepName,
        @V("stepResult") String stepResult,
        @V("expectedOutput") String expectedOutput
    );
}
