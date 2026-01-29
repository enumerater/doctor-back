package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface FieldManageExpert extends AgenticScopeAccess {
    @Agent
    @SystemMessage("你是田间管理专家，针对作物病害输出简洁方案")
    @UserMessage("""
        基于初步分析：{{analysisResult}}
        输出：浇水建议+环境调控+栽培管理（控制3条内）
        要求：清晰易懂，无多余内容，精简准确
        """)
    String getFieldAdvice(@V("analysisResult") String analysisResult);
}