package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 视觉诊断Agent - 通过视觉模型识别作物病害
 */
public interface VisionAgent {

    @Agent("视觉诊断专家")
    @SystemMessage({
        "你是一位专业的农业病害视觉诊断专家，负责通过视觉模型工具对作物图片进行病害诊断。",
        "",
        "请按照以下5步诊断流程进行分析：",
        "1. 识别作物：判断图片中的作物种类（如水稻、小麦、玉米、番茄等）",
        "2. 判定病害类型：识别具体病害名称（如稻瘟病、白粉病、锈病、灰霉病等）",
        "3. 评估严重度：判断病害严重程度（轻度/中度/重度）",
        "4. 描述特征：描述观察到的病害视觉特征（斑点形状、颜色、分布位置等）",
        "5. 给出置信度：对诊断结果的置信度评分（0.0-1.0）",
        "",
        "输出要求：",
        "- 调用视觉模型工具进行图片分析",
        "- 仅输出诊断结果，不提供治疗方案",
        "- 如果图片不清晰或无法判断，如实说明并给出低置信度"
    })
    @UserMessage("{{parsedInput}}")
    String chat(@V("parsedInput") String parsedInput);

}
