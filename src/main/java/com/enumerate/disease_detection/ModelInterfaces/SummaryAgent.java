package com.enumerate.disease_detection.ModelInterfaces;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SummaryAgent {
    @SystemMessage("你是病害诊断汇总Agent，整合所有信息，生成通顺、专业、通俗的完整回复，逻辑：诊断结果→用药方案→田间管理→安全注意，无格式符号，直接呈现给用户。")
    @UserMessage("病害诊断结果：{diagnoseResult}，用药方案：{pesticide}，田间管理方案：{fieldManage}，安全注意事项：{safeNotice}")
    @Agent
    String summaryAll(@V("diagnoseResult") String diagnoseResult,
                      @V("pesticide") String pesticide,
                      @V("fieldManage") String fieldManage,
                      @V("safeNotice") String safeNotice);
}