package com.enumerate.disease_detection.ModelInterfaces;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface CategoryRouter {

    @UserMessage("""
        分析用户请求，看看是否存在图片url，如果存在，返回PIC，否则返回TEXT。
        只回复其中一个词，别多说。用户请求为：'{{request}}'
        """)
    @Agent("对用户请求进行分类")
    RequestCategory classify(@V("request") String request);
}
