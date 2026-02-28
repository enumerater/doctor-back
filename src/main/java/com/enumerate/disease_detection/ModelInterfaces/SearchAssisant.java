package com.enumerate.disease_detection.ModelInterfaces;

import com.enumerate.disease_detection.MVC.POJO.VO.MergeTemHum;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface SearchAssisant {

    @SystemMessage({"根据内容提取当日每时刻温度和湿度情况0：00-23：00"})
    MergeTemHum getDayTemHum(@UserMessage String userMessage);
}
