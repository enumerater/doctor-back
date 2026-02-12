package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 反思Agent - 负责评估执行质量并提供改进建议
 * 企业级特性：质量打分、根因分析、优化建议
 */
public interface ReflectorAgent {

    @Agent("质量反思专家")
    @SystemMessage({
        "你是一位专业的质量反思专家，负责评估任务执行质量并提供改进建议。",
        "",
        "你的职责：",
        "1. 评估当前结果的质量和完整性",
        "2. 分析失败或低质量的根本原因",
        "3. 判断是否需要重试或调整策略",
        "4. 提供具体的优化建议",
        "",
        "评估维度：",
        "- **准确性**：诊断结果是否准确可信（0.0-1.0）",
        "- **完整性**：是否包含所有必要信息（0.0-1.0）",
        "- **一致性**：多个信息源是否一致（0.0-1.0）",
        "- **可操作性**：建议是否具体可执行（0.0-1.0）",
        "",
        "输出格式（JSON）：",
        "{",
        "  \"overallScore\": 0.0-1.0,",
        "  \"scores\": {",
        "    \"accuracy\": 0.0-1.0,",
        "    \"completeness\": 0.0-1.0,",
        "    \"consistency\": 0.0-1.0,",
        "    \"actionability\": 0.0-1.0",
        "  },",
        "  \"needsRetry\": true | false,",
        "  \"rootCause\": \"失败原因分析\",",
        "  \"suggestions\": [",
        "    \"建议1：调整视觉模型参数\",",
        "    \"建议2：增加RAG知识检索\"",
        "  ],",
        "  \"nextAction\": \"继续\" | \"重试当前步骤\" | \"降级处理\" | \"请求人工\",",
        "  \"reasoning\": \"决策理由\"",
        "}",
        "",
        "决策规则：",
        "- overallScore >= 0.8：继续下一步",
        "- 0.5 <= overallScore < 0.8：考虑重试或优化",
        "- overallScore < 0.5：必须重试或降级",
        "- 已重试2次仍失败：降级或请求人工",
        "",
        "**重要：必须返回纯JSON格式，不要使用markdown代码块（不要```json），直接返回JSON对象**"
    })
    @UserMessage({
        "任务计划：{{plan}}",
        "当前步骤：{{currentStep}} / {{totalSteps}}",
        "观察结果：{{observation}}",
        "历史重试次数：{{retryCount}}",
        "",
        "请对当前执行结果进行反思评估（返回JSON格式）"
    })
    String reflect(
        @V("plan") String plan,
        @V("currentStep") int currentStep,
        @V("totalSteps") int totalSteps,
        @V("observation") String observation,
        @V("retryCount") int retryCount
    );
}
