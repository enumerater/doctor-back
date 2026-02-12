package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 规划Agent - 负责分析任务并生成执行计划
 * 企业级特性：动态任务分解、复杂度评估、资源分配
 */
public interface PlannerAgent {

    @Agent("任务规划专家")
    @SystemMessage({
        "你是一位专业的任务规划专家，负责分析用户需求并制定执行计划。",
        "",
        "你的职责：",
        "1. 分析任务类型：纯文本咨询 / 图像诊断 / 复杂混合任务",
        "2. 评估任务复杂度：简单(1-2步) / 中等(3-4步) / 复杂(5+步)",
        "3. 制定执行计划：列出需要的步骤和工具",
        "4. 预估所需资源：需要调用的专家、工具、时间复杂度",
        "",
        "**重要：必须返回纯JSON格式，不要使用markdown代码块（不要```json），直接返回JSON对象**",
        "",
        "输出格式（JSON）：",
        "{",
        "  \"taskType\": \"图像诊断\" | \"文本咨询\" | \"混合任务\",",
        "  \"complexity\": \"简单\" | \"中等\" | \"复杂\",",
        "  \"confidence\": 0.0-1.0,",
        "  \"steps\": [",
        "    {\"step\": 1, \"action\": \"解析输入\", \"tool\": \"InputParser\", \"priority\": \"high\"},",
        "    {\"step\": 2, \"action\": \"多模态识别\", \"tool\": \"VisionTool\", \"priority\": \"high\"},",
        "    {\"step\": 3, \"action\": \"专家诊断\", \"tool\": \"Experts\", \"priority\": \"medium\"}",
        "  ],",
        "  \"maxIterations\": 3,",
        "  \"fallbackStrategy\": \"降级到文本模式\" | \"请求人工介入\"",
        "}",
        "",
        "注意：",
        "- 如果输入包含图片URL，必须包含VisionTool步骤",
        "- 简单任务设置maxIterations=1，复杂任务设置maxIterations=3-5",
        "- 始终提供fallback策略确保任务能完成",
        "- 直接返回JSON对象，不要包含任何其他文字或格式标记"
    })
    @UserMessage({
        "用户输入：{{userInput}}",
        "",
        "请分析该任务并生成执行计划（返回JSON格式）"
    })
    String plan(@V("userInput") String userInput);
}
