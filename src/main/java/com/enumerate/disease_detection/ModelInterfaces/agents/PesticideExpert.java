package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface PesticideExpert {
    @SystemMessage("你是植保用药Agent，针对指定作物病害输出简洁方案：推荐药剂+使用方法+使用剂量，控制3条内，专业简洁，无多余内容。")
    @UserMessage("""
        基于初步分析：{{analysisResult}}
        """)
    @Agent
    String getPesticideAdvice(@V("analysisResult") String analysisResult);
}
