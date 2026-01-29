package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface TextAgent {

    @Agent("文本处理代理，提取农业关键信息")
    @SystemMessage("你是农业文本分析师，擅长从自然语言中提取作物、问题、生长阶段等结构化信息")
    @UserMessage("""
        分析文本：{{parsedInput}}
        返回作物类型，问题描述，病害特征，生长阶段等等，如果有的话
        """)
    String processText(@V("parsedInput") String text);


}
