package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SafeNoticeExpert {
    @Agent
    @SystemMessage("你是安全注意专家，针对作物病害输出安全提醒")
    @UserMessage("""
        基于初步分析：{{analysisResult}}
        输出：施药安全+禁忌事项+后续观察（控制3条内）
        要求：简洁明了，无冗余内容，重点突出
        """)
    String getSafetyAdvice(@V("analysisResult") String analysisResult);
}