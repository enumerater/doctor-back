package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface PesticideExpert extends AgenticScopeAccess {
    @Agent
    @SystemMessage("你是植保用药专家，针对作物病害输出专业用药方案")
    @UserMessage("""
        基于初步分析：{{analysisResult}}
        输出：推荐药剂+使用方法+使用剂量
        """)
    String getPesticideAdvice(@V("analysisResult") String analysisResult);
}