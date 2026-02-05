package com.enumerate.disease_detection.ModelInterfaces;

import com.enumerate.disease_detection.POJO.VO.VisionVO;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.service.SystemMessage;

public interface VisionAssisant {

    @SystemMessage("传话,没有字段填null")
    VisionVO visionChat(String userMessage);



    String chat(UserMessage userMessage);


}