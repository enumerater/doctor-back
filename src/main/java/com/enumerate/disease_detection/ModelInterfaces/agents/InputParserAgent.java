package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.*;

public interface InputParserAgent  {


        @Agent("解析用户文本")
        @UserMessage("""
        分析用户输入：{{request}}
        1. 判断是否包含图片URL（以http/https开头）；
        2. 判断用户输入有无作物类型；
        3. 判断用户输入有无问题描述；
        返回解析结果
        """)
        String parseInput(@V("request") String request);

}