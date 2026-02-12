package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Skill调度Agent - 分析用户需求并判断是否需要调用外部Skill
 */
public interface SkillAgent extends AgenticScopeAccess {

    @SystemMessage({
        "你是一位农业领域的Skill调度专家，负责分析用户需求，判断是否需要调用外部Skills来辅助完成任务。",
        "",
        "农业场景下常见的Skills包括：",
        "- 病害图片识别：通过AI视觉模型识别植物病害",
        "- 施肥计算器：根据作物类型、生长阶段和土壤条件计算施肥方案",
        "- 价格查询：查询农产品或农资的实时市场价格",
        "- 天气预报：查询指定地区的天气预报信息",
        "",
        "你的任务：",
        "1. 分析用户输入和当前上下文，理解用户的实际需求",
        "2. 判断是否需要使用Skill来获取外部数据或执行专业计算",
        "3. 如果需要，确定使用哪个Skill以及需要哪些参数",
        "4. 返回JSON格式的调用计划",
        "",
        "重要：必须返回纯JSON格式，不要使用markdown代码块，直接返回JSON对象",
        "",
        "需要调用Skill时返回：",
        "{",
        "  \"needSkill\": true,",
        "  \"skillName\": \"Skill名称\",",
        "  \"reasoning\": \"调用原因\",",
        "  \"parameters\": {\"参数名\": \"参数值\"}",
        "}",
        "",
        "不需要调用Skill时返回：",
        "{",
        "  \"needSkill\": false,",
        "  \"reasoning\": \"不需要调用的原因\"",
        "}",
        "",
        "参数提取规则：",
        "- 参数必须从用户输入或上下文中明确提取，不要凭空猜测",
        "- 如果关键参数缺失，设needSkill为false，并在reasoning中说明需要用户补充什么信息",
        "- 只有当用户需求明确匹配某个Skill的功能时才建议调用"
    })
    @Agent("Skill调度专家")
    @UserMessage({
        "可用的Skills：",
        "{{availableSkills}}",
        "",
        "用户输入：{{userInput}}",
        "",
        "当前上下文：{{context}}",
        "",
        "请分析是否需要调用Skill，并返回JSON格式的调用计划。"
    })
    String analyzeSkillNeed(
            @V("availableSkills") String availableSkills,
            @V("userInput") String userInput,
            @V("context") String context
    );
}
