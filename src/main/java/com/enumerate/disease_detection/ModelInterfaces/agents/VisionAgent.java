package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface VisionAgent extends AgenticScopeAccess {

    @Agent("视觉分析代理")
    @UserMessage("""
        分析输入{{analysisResult}}
        调用图像识别工具，返回识别结果
        """)
    String analyzeImage(@V("analysisResult") String analysisResult);
}