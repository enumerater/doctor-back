package com.enumerate.disease_detection.MVC.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enumerate.disease_detection.ChatModel.MainModel;
import com.enumerate.disease_detection.Local.UserContextHolder;
import com.enumerate.disease_detection.MVC.Mapper.AnnouncementMapper;
import com.enumerate.disease_detection.MVC.Mapper.NotificationMapper;
import com.enumerate.disease_detection.MVC.Mapper.UserMapper;
import com.enumerate.disease_detection.MVC.POJO.DTO.AnnouncementGenerateDTO;
import com.enumerate.disease_detection.MVC.POJO.DTO.AnnouncementPublishDTO;
import com.enumerate.disease_detection.MVC.POJO.PO.AnnouncementPO;
import com.enumerate.disease_detection.MVC.POJO.PO.NotificationPO;
import com.enumerate.disease_detection.MVC.POJO.PO.UserPO;
import com.enumerate.disease_detection.MVC.POJO.VO.AnnounceTypes;
import com.enumerate.disease_detection.MVC.POJO.VO.AnnouncementGenerateVO;
import com.enumerate.disease_detection.MVC.POJO.VO.AnnouncementPageQueryVO;
import com.enumerate.disease_detection.MVC.POJO.VO.AnnouncementReadVO;
import com.enumerate.disease_detection.ModelInterfaces.AnnouncementGenerate;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Resource(name = "tongYiModel")
    private OpenAiChatModel openAiChatModel;

    public AnnouncementGenerateVO generateAnnouncement(AnnouncementGenerateDTO announcementGenerateDTO) {

        AnnouncementGenerate aiServices = AiServices.builder(AnnouncementGenerate.class)
                .chatModel(openAiChatModel)
                .build();

        return aiServices.generateAnnouncement(announcementGenerateDTO.getPrompt());
    }

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

    public AnnouncementPO publishAnnouncement(AnnouncementPublishDTO announcementPublishDTO) {
        AnnouncementPO announcementPO = AnnouncementPO.builder()
                .title(announcementPublishDTO.getTitle())
                .content(announcementPublishDTO.getContent())
                .type(announcementPublishDTO.getType())
                .priority(announcementPublishDTO.getPriority())
                .targetUsers(announcementPublishDTO.getTargetUsers())
                .status("published")
                .publishedAt(String.valueOf(LocalDateTime.now()))
                .build();

        announcementMapper.insert(announcementPO);

        List<UserPO> userPO = userMapper.selectList(null);
        for (UserPO user : userPO) {
            NotificationPO notificationPO = NotificationPO.builder()
                    .userId(user.getId())
                    .type(announcementPublishDTO.getType())
                    .title(announcementPublishDTO.getTitle())
                    .content(announcementPublishDTO.getContent())
                    .priority(announcementPublishDTO.getPriority())
                    .link("")
                    .isRead( false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationMapper.insert(notificationPO);
        }

        return announcementPO;

    }

    public AnnouncementPageQueryVO listAnnouncements(Integer page, Integer pageSize, String type,  String keyword) {
        if (page == null || page < 1){
            page = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100){
            pageSize = 20;
        }

        Page<AnnouncementPO> pageParam = new Page<>(page, pageSize);

        QueryWrapper<AnnouncementPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(type != null, "type", type);
        queryWrapper.like(keyword != null, "title", keyword);


        Page<AnnouncementPO> announcementPOPage = announcementMapper.selectPage(pageParam, queryWrapper);
        return new AnnouncementPageQueryVO(announcementPOPage.getRecords(), (int) announcementPOPage.getTotal());
    }

}
