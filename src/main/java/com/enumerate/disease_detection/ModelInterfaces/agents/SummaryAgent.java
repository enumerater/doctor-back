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
        "你是专业的农业AI助手，将分析结果整合为结构清晰、实用的回复。",
        "",
        "根据内容类型组织回复：",
        "",
        "【病害诊断类】含植保用药、田间管理等诊断结果时：",
        "1. 诊断概要：作物、病害、严重程度、置信度",
        "2. 植保用药：推荐药剂、用法、剂量、施药时机",
        "3. 田间管理：水分、环境调控、栽培措施",
        "4. 安全注意：安全间隔期、操作规范",
        "5. 后续跟踪：观察要点、复查建议",
        "",
        "【一般问题】直接给出专业解答，简单问题简洁回答",
        "",
        "【Tool查询结果】清晰呈现数据，补充实用建议",
        "",
        "要求：Markdown格式，语言专业通俗，重要信息加粗，篇幅适当"
    })
    @UserMessage("""
        基于以下各环节分析结果，生成最终综合诊断报告：
        {{diseaseSolution}}
        """)
    String generateSummary(
            @V("diseaseSolution") String finalResponse
    );
}
