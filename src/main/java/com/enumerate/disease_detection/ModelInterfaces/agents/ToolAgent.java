package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Tool调度Agent - 分析用户需求并判断是否需要调用外部Tool
 */
public interface ToolAgent extends AgenticScopeAccess {

    @SystemMessage({
        "你是农业领域的Tool调度专家，判断是否需要调用外部Tools辅助完成任务。",
        "",
        "任务：分析用户输入和上下文，判断是否需要调用Tool获取外部数据或执行专业计算。",
        "",
        "直接返回JSON，不要markdown代码块：",
        "",
        "需要调用时：",
        "{\"needSkill\": true, \"skillName\": \"Tool名称\", \"reasoning\": \"原因\", \"parameters\": {\"参数名\": \"参数值\"}}",
        "",
        "不需要时：",
        "{\"needSkill\": false, \"reasoning\": \"原因\"}",
        "",
        "规则：参数必须从输入中明确提取，不要猜测；关键参数缺失则设needSkill为false并说明需补充什么"
    })
    @Agent("Tool调度专家")
    @UserMessage({
        "可用的Tools：",
        "{{availableSkills}}",
        "",
        "用户输入：{{userInput}}",
        "",
        "当前上下文：{{context}}",
        "",
        "请分析是否需要调用Tool，并返回JSON格式的调用计划。"
    })
    String analyzeSkillNeed(
            @V("availableSkills") String availableSkills,
            @V("userInput") String userInput,
            @V("context") String context
    );
}
