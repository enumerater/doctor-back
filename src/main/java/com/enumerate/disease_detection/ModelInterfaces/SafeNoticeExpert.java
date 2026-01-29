package com.enumerate.disease_detection.ModelInterfaces;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SafeNoticeExpert {
    @SystemMessage("你是安全注意Agent，针对指定作物病害输出简洁提醒：施药安全+禁忌事项+后续观察，控制3条内，无冗余内容。")
    @UserMessage("作物：{crop}，病害：{disease}")
    @Agent
    String getSafeNotice(@V("crop") String crop, @V("disease") String disease);
}
