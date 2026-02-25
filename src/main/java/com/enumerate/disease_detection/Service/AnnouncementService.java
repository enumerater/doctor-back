package com.enumerate.disease_detection.Service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enumerate.disease_detection.ChatModel.MainModel;
import com.enumerate.disease_detection.Mapper.AnnouncementMapper;
import com.enumerate.disease_detection.ModelInterfaces.AnnouncementGenerate;
import com.enumerate.disease_detection.POJO.DTO.AnnouncementGenerateDTO;
import com.enumerate.disease_detection.POJO.DTO.AnnouncementPublishDTO;
import com.enumerate.disease_detection.POJO.PO.AnnouncementPO;
import com.enumerate.disease_detection.POJO.VO.AnnouncementGenerateVO;
import com.enumerate.disease_detection.POJO.VO.AnnouncementPageQueryVO;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Autowired
    private MainModel mainModel;

    public AnnouncementGenerateVO generateAnnouncement(AnnouncementGenerateDTO announcementGenerateDTO) {
        OpenAiChatModel openAiChatModel = mainModel.tongYiModel();

        AnnouncementGenerate aiServices = AiServices.builder(AnnouncementGenerate.class)
                .chatModel(openAiChatModel)
                .build();

        return aiServices.generateAnnouncement(announcementGenerateDTO.getPrompt());
    }


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

        return announcementPO;

    }

    public AnnouncementPageQueryVO listAnnouncements(Integer page, Integer pageSize, String type) {
        if (page == null || page < 1){
            page = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100){
            pageSize = 20;
        }

        Page<AnnouncementPO> pageParam = new Page<>(page, pageSize);

        QueryWrapper<AnnouncementPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(type != null, "type", type);

        Page<AnnouncementPO> announcementPOPage = announcementMapper.selectPage(pageParam, queryWrapper);
        return new AnnouncementPageQueryVO(announcementPOPage.getRecords(), (int) announcementPOPage.getTotal());
    }
}
