package com.enumerate.disease_detection.ModelInterfaces;

import com.enumerate.disease_detection.MVC.POJO.VO.VisionVO;
import dev.langchain4j.service.SystemMessage;

public interface VisionAssisant {

    @SystemMessage("传话,没有字段填null")
    VisionVO visionChat(String userMessage);



    String chat(String userMessage);


}