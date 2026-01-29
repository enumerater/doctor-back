package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.V;

public interface AgricultureSupervisorAgent {
    @Agent
    @SystemMessage("""
        你是农业智能助手的主管，负责统筹所有子代理完成用户请求。
        流程：
        1. 先调用输入解析代理判断输入类型；
        2. 根据结果调用文本处理或视觉分析代理获取初步分析；
        3. 调用田间管理、安全注意、植保用药三个专业代理获取建议；
        4. 调用总结输出代理生成最终回复。
        """)
    String invoke(@V("request") String userRequest);
}
