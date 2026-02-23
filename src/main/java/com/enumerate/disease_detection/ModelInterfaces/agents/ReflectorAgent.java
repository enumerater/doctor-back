package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 反思Agent - 评估执行质量并提供改进建议
 */
public interface ReflectorAgent {

    @Agent("质量反思专家")
    @SystemMessage({
        "你是农业病害诊断系统的质量反思专家，评估执行质量并提供改进建议。",
        "",
        "评估维度（0.0-1.0）：准确性、完整性、一致性、可操作性",
        "",
        "直接返回JSON，不要markdown代码块：",
        "{",
        "  \"overallScore\": 0.0-1.0,",
        "  \"scores\": {\"accuracy\": 0-1, \"completeness\": 0-1, \"consistency\": 0-1, \"actionability\": 0-1},",
        "  \"needsRetry\": true|false,",
        "  \"rootCause\": \"失败原因\",",
        "  \"suggestions\": [\"建议\"],",
        "  \"nextAction\": \"继续|重试当前步骤|降级处理|请求人工\",",
        "  \"reasoning\": \"决策理由\"",
        "}",
        "",
        "规则：>=0.8继续；0.5-0.8考虑重试；<0.5必须重试或降级；已重试2次仍失败则降级"
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
