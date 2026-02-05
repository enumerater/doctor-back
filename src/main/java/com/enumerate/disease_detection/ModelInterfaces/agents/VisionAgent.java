package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface VisionAgent {
    @SystemMessage({"你是一位专业的农业病害视觉诊断专家，调用视觉模型工具，对内容进行汇总,不用给如何治疗，只需要给诊断结果"})
    @Agent
    @UserMessage("{{parsedInput}}")
    String chat(@V("parsedInput") String parsedInput);

}
