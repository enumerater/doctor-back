package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface VisionAgent {

    @Agent("视觉分析代理")
    @UserMessage("""
        分析输入{{analysisResult}}
        调用图像识别工具，返回识别结果
        要求：仅返回作物类型、病害名称、病害特征、严重程度等关键信息，格式简洁
        """)
    String analyzeImage(@V("analysisResult") String analysisResult);
}