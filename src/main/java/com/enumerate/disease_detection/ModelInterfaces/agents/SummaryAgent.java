package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SummaryAgent {
    @Agent("总结输出代理")
    @UserMessage("""
        基于以下内容生成最终回复：
        {{diseaseSolution}}
        要求：
        1. 结构清晰，分点明确
        2. 内容精简，仅保留关键信息
        3. 语言专业友好，易于理解
        4. 避免冗余描述，减少无意义词汇
        5. 控制回复长度，尽量缩短token数
        """)
    String generateSummary(
            @V("diseaseSolution") String finalResponse
    );
}