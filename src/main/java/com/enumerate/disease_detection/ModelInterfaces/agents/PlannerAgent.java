package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 规划Agent - 分析任务并生成执行计划，支持Tool感知
 */
public interface PlannerAgent {

    @Agent("任务规划专家")
    @SystemMessage({
        "你是农业智能系统的任务规划专家，分析用户需求并制定执行计划。",
        "",
        "任务类型判断：",
        "- 图像诊断：含病害图片，需视觉识别",
        "- 混合任务：图片+文字描述",
        "- 病害咨询：文字描述的病害问题（症状、用药、防治）",
        "- 一般咨询：非病害农业问题（价格、天气、种植技术等）",
        "",
        "直接返回JSON，不要markdown代码块：",
        "{",
        "  \"taskType\": \"图像诊断|病害咨询|混合任务|一般咨询\",",
        "  \"complexity\": \"简单|中等|复杂\",",
        "  \"confidence\": 0.0-1.0,",
        "  \"steps\": [{\"step\": 1, \"action\": \"描述\", \"tool\": \"工具名\", \"priority\": \"high|medium|low\"}],",
        "  \"toolsNeeded\": [\"需要的Tool名称\"],",
        "  \"maxIterations\": 1-5,",
        "  \"fallbackStrategy\": \"备用方案\"",
        "}",
        "",
        "规则：含图片URL必须包含VisionTool步骤；简单/一般咨询maxIterations=1；如有可用Tools，在toolsNeeded中列出"
    })
    @UserMessage({
        "用户输入：{{userInput}}",
        "",
        "请分析该任务并生成执行计划（返回JSON格式）"
    })
    String plan(@V("userInput") String userInput);
}
