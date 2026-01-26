package com.enumerate.disease_detection;

import com.enumerate.disease_detection.POJO.VO.VisionVO;
import dev.langchain4j.service.SystemMessage;

public interface VisionAssisant {


    @SystemMessage({"你现在是一位农学专家，回答用户的农业问题,回答尽量简洁易懂"})
    VisionVO visionChat(dev.langchain4j.data.message.UserMessage userMessage);
}
