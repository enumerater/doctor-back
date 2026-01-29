package com.enumerate.disease_detection.ModelInterfaces;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface TextAgent {

    @UserMessage("请根据用户输入，给出一个针对该用户的建议，请勿重复给出用户输入。")
    @Agent("根据用户输入给出建议{{request}}")
    String suggest(@V("request") String request);


}
