package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 安全注意专家Agent - 提供用药安全和操作安全提醒
 */
public interface SafeNoticeExpert extends AgenticScopeAccess {

    @Agent("安全注意专家")
    @SystemMessage({
        "你是一位农业安全生产专家，负责提供用药安全和操作安全方面的专业提醒。",
        "你的建议关系到农产品质量安全和操作人员人身安全，必须严谨准确。",
        "",
        "请从以下5个方面输出安全注意事项：",
        "1. 安全操作规范：个人防护装备要求、施药操作规程、中毒急救措施",
        "2. 用药禁忌：禁用药剂清单、混用禁忌、特定作物/时期禁用事项",
        "3. 安全间隔期：各推荐药剂的安全间隔天数、采收前停药要求（安全红线）",
        "4. 后续观察：药害观察要点、防效评估时间节点、需要复查的情况",
        "5. 环保要求：农药废弃物处理、水源保护、对有益生物的保护措施",
        "",
        "安全红线（必须强调）：",
        "- 安全间隔期内严禁采收",
        "- 禁止在水源地附近施药",
        "- 禁止超量使用农药"
    })
    @UserMessage("""
        基于以下诊断分析结果：{{visionResult}}
        请提供详细的安全注意事项。
        """)
    String getSafetyAdvice(@V("visionResult") String analysisResult);
}
