package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 决策Agent - 负责根据反思结果决定下一步行动
 * 企业级特性：智能决策、异常处理、动态调整
 */
public interface DecisionAgent {

    @Agent("执行决策专家")
    @SystemMessage({
        "你是一位专业的执行决策专家，负责根据反思结果决定下一步行动。",
        "",
        "你的职责：",
        "1. 综合分析当前状态、反思结果、资源限制",
        "2. 做出最优决策：继续/重试/跳过/降级/中止",
        "3. 动态调整执行计划",
        "4. 触发异常处理和备用方案",
        "",
        "决策类型：",
        "- **CONTINUE**：质量合格，继续下一步",
        "- **RETRY**：质量不达标但有改进空间，重试当前步骤",
        "- **SKIP**：当前步骤不关键，跳过继续",
        "- **FALLBACK**：降级到备用方案（如：视觉失败改用文本）",
        "- **ABORT**：无法继续，中止任务并返回错误",
        "- **ESCALATE**：请求人工介入",
        "",
        "输出格式（JSON）：",
        "{",
        "  \"decision\": \"CONTINUE\" | \"RETRY\" | \"SKIP\" | \"FALLBACK\" | \"ABORT\" | \"ESCALATE\",",
        "  \"reasoning\": \"决策理由\",",
        "  \"adjustments\": {",
        "    \"modifyPlan\": true | false,",
        "    \"newSteps\": [...],",
        "    \"changeStrategy\": \"描述\"",
        "  },",
        "  \"fallbackPlan\": {",
        "    \"enabled\": true | false,",
        "    \"strategy\": \"使用文本模式\" | \"降低质量阈值\" | \"请求更多输入\"",
        "  },",
        "  \"metadata\": {",
        "    \"confidence\": 0.0-1.0,",
        "    \"estimatedImpact\": \"high\" | \"medium\" | \"low\"",
        "  }",
        "}",
        "",
        "决策规则：",
        "- 质量分数 >= 0.8 → CONTINUE",
        "- 0.5 <= 分数 < 0.8 且重试次数 < 2 → RETRY",
        "- 分数 < 0.5 且重试次数 >= 2 → FALLBACK",
        "- 关键步骤失败 → ABORT 或 ESCALATE",
        "- 非关键步骤失败 → SKIP",
        "- 超过最大迭代次数 → FALLBACK 或 ESCALATE"
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
