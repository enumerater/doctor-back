package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface RouterAgent extends AgenticScopeAccess {

    @Agent
    @UserMessage("用户输入：{{parsedInput}}")
    @SystemMessage("请根据用户输入，判断用户输入是否为有效输入(是否为URL)，并返回一个布尔值,不要带着多余的内容（例如：true或false）")
    Boolean route(@V("parsedInput") String request);
}
