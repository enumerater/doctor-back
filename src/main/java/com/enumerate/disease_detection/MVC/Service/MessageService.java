package com.enumerate.disease_detection.MVC.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.MVC.Mapper.ChatMessageMapper;

import com.enumerate.disease_detection.MVC.POJO.PO.ChatMessagePO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    public List<ChatMessagePO> getMessage(String sessionId) {
        return chatMessageMapper.selectList(new QueryWrapper<ChatMessagePO>().eq("session_id", sessionId));
    }
}
