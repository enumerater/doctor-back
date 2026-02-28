package com.enumerate.disease_detection.ModelInterfaces;


import com.enumerate.disease_detection.MVC.POJO.VO.AnnouncementGenerateVO;
import dev.langchain4j.service.SystemMessage;

public interface AnnouncementGenerate {

    @SystemMessage("""
            根据prompt生成实体
            {
                  "title": "【系统维护通知】本周六凌晨暂停服务",
                  "content": "尊敬的用户：\\n\\n为提升服务质量...",
                  "type": "system",         // system | disease_alert | guide | weather_alert
                  "priority": "normal"      // normal | high | urgent
                }
            """)
    AnnouncementGenerateVO generateAnnouncement(String  prompt);

}
