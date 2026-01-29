package com.enumerate.disease_detection.ModelInterfaces;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SchemeSplitAgent {
    @SystemMessage("你是拆解助手，仅做1件事：将输入的病害识别结果按|分割，提取第1位（作物）和第2位（病害），按 作物=XXX|病害=XXX 格式输出，无多余内容。")
    @UserMessage("病害识别结果：{response}")
    @Agent
    String split(@V("response") String response);
}
