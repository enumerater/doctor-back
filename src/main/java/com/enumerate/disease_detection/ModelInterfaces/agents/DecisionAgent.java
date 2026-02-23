package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 决策Agent - 根据反思结果决定下一步行动
 */
public interface DecisionAgent {

    @Agent("执行决策专家")
    @SystemMessage({
        "你是农业病害诊断系统的执行决策专家，根据反思结果决定下一步行动。",
        "",
        "决策类型：CONTINUE(质量合格)、RETRY(需重试)、SKIP(跳过非关键)、FALLBACK(降级)、ABORT(中止)、ESCALATE(请求人工)",
        "",
        "直接返回JSON，不要markdown代码块：",
        "{",
        "  \"decision\": \"CONTINUE|RETRY|SKIP|FALLBACK|ABORT|ESCALATE\",",
        "  \"reasoning\": \"决策理由\",",
        "  \"adjustments\": {\"modifyPlan\": true|false, \"newSteps\": [\"步骤\"], \"changeStrategy\": \"描述\"},",
        "  \"fallbackPlan\": {\"enabled\": true|false, \"strategy\": \"策略\"},",
        "  \"metadata\": {\"confidence\": 0-1, \"estimatedImpact\": \"high|medium|low\"}",
        "}",
        "",
        "规则：>=0.8→CONTINUE；0.5-0.8且重试<2→RETRY；<0.5且重试>=2→FALLBACK；关键步骤失败→ABORT/ESCALATE"
    })
    @UserMessage({
        "执行计划：{{plan}}",
        "当前进度：{{currentStep}} / {{totalSteps}}",
        "反思结果：{{reflection}}",
        "重试次数：{{retryCount}} / {{maxRetries}}",
        "已用迭代：{{iteration}} / {{maxIterations}}",
        "",
        "请做出执行决策（返回JSON格式）"
    })
    String decide(
        @V("plan") String plan,
        @V("currentStep") int currentStep,
        @V("totalSteps") int totalSteps,
        @V("reflection") String reflection,
        @V("retryCount") int retryCount,
        @V("maxRetries") int maxRetries,
        @V("iteration") int iteration,
        @V("maxIterations") int maxIterations
    );
}
