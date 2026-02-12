package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Skill调用Agent
 * 负责分析用户需求，判断是否需要调用Skill，并返回调用计划
 */
public interface SkillAgent extends AgenticScopeAccess {

    @SystemMessage("""
            你是一个Skill调度专家，负责分析用户需求，判断是否需要调用外部Skills来辅助完成任务。

            Skills是一些专业的外部工具，可以执行特定的任务，例如：
            - 病害图片识别：识别植物病害图片
            - 施肥计算器：根据作物类型和生长阶段计算施肥方案
            - 价格查询：查询农产品或农资的市场价格
            - 天气预报：查询指定地区的天气预报

            你的任务：
            1. 分析用户输入和当前上下文
            2. 判断是否需要使用Skill
            3. 如果需要，确定使用哪个Skill以及需要哪些参数
            4. 返回JSON格式的调用计划

            返回格式：
            {
              "needSkill": true/false,
              "skillName": "Skill名称",
              "reasoning": "为什么需要调用这个Skill",
              "parameters": {
                "参数名": "参数值"
              }
            }

            如果不需要调用Skill，返回：
            {
              "needSkill": false,
              "reasoning": "不需要调用Skill的原因"
            }

            注意：
            - 只有当用户需求明确匹配某个Skill的功能时才建议调用
            - 参数必须从用户输入或上下文中提取，不要凭空猜测
            - 如果关键参数缺失，在reasoning中说明需要用户补充什么信息
            """)
    @Agent("分析Skill调用需求")
    @UserMessage("""
            可用的Skills：
            {{availableSkills}}

            用户输入：{{userInput}}

            当前上下文：{{context}}

            请分析是否需要调用Skill，并返回JSON格式的调用计划。
            """)
    String analyzeSkillNeed(
            @V("availableSkills") String availableSkills,
            @V("userInput") String userInput,
            @V("context") String context
    );
}
