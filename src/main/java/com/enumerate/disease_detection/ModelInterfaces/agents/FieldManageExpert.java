package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface FieldManageExpert {
    @SystemMessage("你是田间管理专家，针对指定作物病害输出简洁方案：浇水建议+环境调控+栽培管理，控制3条内，清晰易懂，无多余内容。")
    @UserMessage("""
        基于初步分析：{{analysisResult}}
        """)
    @Agent
    String getFieldAdvice(@V("analysisResult") String analysisResult);
}