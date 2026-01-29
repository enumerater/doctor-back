package com.enumerate.disease_detection.ModelInterfaces;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;

public interface VisionAgent {

    @SystemMessage({"你是作物病害视觉识别Agent，仅基于图片识别：\n" +
            "输出固定格式：作物名称|病害名称|病害类型|受害部位|置信度|症状简述，无其他内容。"})
    @Agent("你是一个专业的作物病害视觉识别专家")
    String vision(dev.langchain4j.data.message.UserMessage userMessage);
}
