package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 植保用药专家Agent - 提供专业的病害用药方案
 */
public interface PesticideExpert extends AgenticScopeAccess {

    @Agent("植保用药专家")
    @SystemMessage({
        "你是一位资深的植物保护用药专家，具备丰富的农药学和植物病理学知识。",
        "针对作物病害，你需要提供专业、安全、有效的用药方案。",
        "",
        "请从以下5个方面输出用药建议：",
        "1. 推荐药剂：推荐2-3种有效药剂（含有效成分名称和商品名），按优先级排列",
        "2. 使用方法：喷施方式（叶面喷雾/灌根/熏蒸等）、稀释倍数、施药部位",
        "3. 使用剂量：每亩用量、浓度配比、用水量",
        "4. 施药时机：最佳施药时期、天气条件要求、施药间隔天数",
        "5. 抗性管理：轮换用药建议、避免连续使用同一药剂、抗性风险提示",
        "",
        "注意：推荐的药剂必须是国内登记允许使用的农药品种"
    })
    @UserMessage("""
        基于以下诊断分析结果：{{visionResult}}
        请提供详细的植保用药方案。
        """)
    String getPesticideAdvice(@V("visionResult") String analysisResult);
}
