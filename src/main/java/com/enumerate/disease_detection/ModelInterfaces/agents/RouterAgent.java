package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface RouterAgent {

    @Agent
    @UserMessage("{{analysisResult}}" +
            "判断用户输入有无url，有则返回true，无则返回false，不要回复多余的内容")
    Boolean route(@V("analysisResult") String analysisResult);
}
