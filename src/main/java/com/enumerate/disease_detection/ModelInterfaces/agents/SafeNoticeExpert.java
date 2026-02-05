package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SafeNoticeExpert extends AgenticScopeAccess {
    @Agent
    @SystemMessage("你是安全注意专家，针对作物病害输出简洁安全提醒")
    @UserMessage("""
        基于初步分析：{{visionResult}}
        输出：施药安全+禁忌事项+后续观察
        """)
    String getSafetyAdvice(@V("visionResult") String analysisResult);
}