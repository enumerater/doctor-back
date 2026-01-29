package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;

public interface PlannerAgent {

    @UserMessage("""
        你是计划专家，根据用户输入{{analysisResult}}，输出简洁的计划方案：安全注意，植保用药，田间管理
        """)
    @Agent
    String plan(
            @P("analysisResult") String analysisResult
    );

}
