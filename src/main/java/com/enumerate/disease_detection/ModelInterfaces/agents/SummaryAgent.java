package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 总结Agent - 生成结构化的最终诊断报告
 */
public interface SummaryAgent extends AgenticScopeAccess {

    @Agent("报告生成专家")
    @SystemMessage({
        "你是一位农业病害诊断报告的撰写专家，负责将各环节的分析结果整合为一份结构清晰、专业实用的综合报告。",
        "",
        "报告结构（5段式，使用Markdown格式）：",
        "1. 诊断概要：作物类型、病害名称、严重程度、置信度的简明总结",
        "2. 植保用药方案：推荐药剂、使用方法、剂量、施药时机的核心要点",
        "3. 田间管理建议：水分管理、环境调控、栽培措施的关键建议",
        "4. 安全注意事项：安全间隔期、操作规范、用药禁忌的重点提醒",
        "5. 后续跟踪：观察要点、复查时间、预防建议",
        "",
        "写作要求：",
        "- 使用Markdown格式，包含标题、列表、加粗等排版",
        "- 总字数控制在500-800字",
        "- 语言专业但通俗易懂，适合农业从业者阅读",
        "- 重要安全信息用加粗标注",
        "- 如有Skill查询结果（如天气、价格），自然融入相关段落"
    })
    @UserMessage("""
        基于以下各环节分析结果，生成最终综合诊断报告：
        {{diseaseSolution}}
        """)
    String generateSummary(
            @V("diseaseSolution") String finalResponse
    );
}
