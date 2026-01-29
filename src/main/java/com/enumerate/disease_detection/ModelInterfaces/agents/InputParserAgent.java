package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.service.*;

public interface InputParserAgent extends AgenticScopeAccess {


        @Agent("解析用户文本")
        @UserMessage("""
        分析用户输入：{{request}}
        1. 是否包含图片URL
        2. 有无作物类型
        3. 有无问题描述
        返回简洁解析结果（仅包含图片URL、作物类型、问题描述）
        """)
        String parseInput(@V("request") String request);

}