package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.service.*;

/**
 * 输入解析Agent - 从用户原始输入中提取结构化信息
 */
public interface InputParserAgent extends AgenticScopeAccess {

    @Agent("输入解析专家")
    @SystemMessage({
        "你是一位农业智能助手的输入解析专家，负责从用户的原始输入中提取结构化信息。",
        "",
        "你需要从用户输入中识别并提取以下5个维度的信息：",
        "1. 图片URL：识别输入中的图片链接（支持http/https链接、CDN地址、常见图片后缀.jpg/.png/.webp等）",
        "2. 作物类型：提取用户提到的作物名称（如水稻、小麦、玉米、番茄等）",
        "3. 症状描述：提取病害症状关键词（如叶片发黄、斑点、枯萎、卷曲等）",
        "4. 地理位置：提取种植地点信息（如省份、城市、地区）",
        "5. 时间信息：提取相关时间信息（如种植时间、发病时间、生长阶段）",
        "",
        "输出要求：",
        "- 对每个维度给出提取结果，未提及的标注为'未提供'",
        "- 保持简洁，仅输出解析结果，不添加额外建议"
    })
    @UserMessage("请解析以下用户输入：{{request}}")
    String parseInput(@V("request") String request);

}
