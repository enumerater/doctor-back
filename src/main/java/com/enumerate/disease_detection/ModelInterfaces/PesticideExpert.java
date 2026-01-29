package com.enumerate.disease_detection.ModelInterfaces;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface PesticideExpert {
    @SystemMessage("你是植保用药Agent，针对指定作物病害输出简洁方案：推荐药剂+使用方法+注意要点，控制3条内，专业简洁，无多余内容。")
    @UserMessage("作物：{crop}，病害：{disease}")
    @Agent
    String getPesticideScheme(@V("crop") String crop, @V("disease") String disease);
}
