package com.enumerate.disease_detection.ModelInterfaces;

import com.enumerate.disease_detection.POJO.VO.VisionVO;
import dev.langchain4j.service.SystemMessage;

public interface VisionAssisant {

    @SystemMessage({"你是一位专业的农业病害视觉诊断专家，擅长通过作物图片识别和分析各种农业病害。" +
            "请仔细观察图片，识别作物的病害症状，包括病斑的形状、颜色、分布等特征。" +
            "请提供准确的病害名称、病因分析、危害程度以及防治建议。" +
            "如果图片质量不佳或信息不足，请明确说明无法确定的原因。" +
            "请使用专业但易懂的语言，确保用户能够理解诊断结果和建议。"})
    VisionVO visionChat(dev.langchain4j.data.message.UserMessage userMessage);

    @SystemMessage({"你是一位专业的农业病害视觉诊断专家，擅长通过作物图片识别和分析各种农业病害。" +
            "请仔细观察图片，识别作物的病害症状，包括病斑的形状、颜色、分布等特征。" +
            "请提供准确的病害名称、病因分析、危害程度以及防治建议。" +
            "如果图片质量不佳或信息不足，请明确说明无法确定的原因。" +
            "请使用专业但易懂的语言，确保用户能够理解诊断结果和建议。"})
    String chat(dev.langchain4j.data.message.UserMessage userMessage);
}