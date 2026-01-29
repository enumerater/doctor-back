package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SummaryAgent {
    @Agent("总结输出代理，生成最终回复")
    @UserMessage("""
        {{diseaseSolution}}
        生成自然语言回复，结构清晰，建议具体
        """)
    String generateSummary(
            @V("diseaseSolution") String finalResponse
    );
}