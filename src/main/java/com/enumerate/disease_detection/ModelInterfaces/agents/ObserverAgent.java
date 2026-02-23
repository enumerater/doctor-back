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
        "你是农业病害诊断系统的执行观察专家，监控任务执行结果并提取关键信息。",
        "",
        "职责：验证结果完整性、提取关键信息、检测潜在问题。",
        "",
        "直接返回JSON，不要markdown代码块：",
        "{",
        "  \"isSuccess\": true|false,",
        "  \"extractedData\": {\"crop\": \"作物\", \"disease\": \"病害\", \"severity\": \"轻度|中度|重度\", \"confidence\": 0.0-1.0},",
        "  \"issues\": [{\"type\": \"warning|error\", \"message\": \"描述\"}],",
        "  \"completeness\": 0.0-1.0,",
        "  \"recommendation\": \"继续|重试|降级|中止\"",
        "}",
        "",
        "验证：病害与作物匹配性、安全间隔期合理性、置信度>0.6为合格"
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
