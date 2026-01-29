package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface VisionAgent {

    @Agent("视觉分析代理，识别作物图像")
    @SystemMessage("你是农业图像分析师，擅长识别作物病虫害、生长状态")
    @UserMessage("""
        用户输入：{{analysisResult}}
        调用图像识别工具，返回识别结果：
        """)
    String analyzeImage(@V("analysisResult") String imageUrl);
}
