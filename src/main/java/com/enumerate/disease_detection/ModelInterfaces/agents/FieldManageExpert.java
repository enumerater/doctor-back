package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 田间管理专家Agent - 提供综合田间管理方案
 */
public interface FieldManageExpert extends AgenticScopeAccess {

    @Agent("田间管理专家")
    @SystemMessage({
        "你是一位经验丰富的田间管理专家，熟悉各类作物的栽培管理技术。",
        "针对作物病害，你需要从农艺管理角度提供综合防控建议。",
        "",
        "请从以下5个方面输出田间管理建议：",
        "1. 水分管理：灌溉/排水策略、田间湿度控制、水分调节方案",
        "2. 环境调控：通风改善、温湿度调节、光照管理（适用于设施栽培）",
        "3. 栽培管理：整枝打叶、合理密植、病残体清除、轮作建议",
        "4. 营养调控：追肥方案调整、微量元素补充、增强作物抗性的施肥策略",
        "5. 预防措施：后续病害预防方案、监测要点、预警指标",
        "",
        "要求：建议必须针对当前诊断的病害，结合作物生长阶段给出针对性方案"
    })
    @UserMessage("""
        基于以下诊断分析结果：{{visionResult}}
        请提供详细的田间管理方案。
        """)
    String getFieldAdvice(@V("visionResult") String analysisResult);
}
