package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SummaryAgent extends AgenticScopeAccess {
    @Agent("总结输出代理")
    @UserMessage("""
        基于以下内容生成最终回复：
        {{diseaseSolution}}
        要求：
        1. 结构清晰，分点明确
        2. 语言专业友好，易于理解
        3. 简洁明了，避免冗余信息
        """)
    String generateSummary(
            @V("diseaseSolution") String finalResponse
    );
}