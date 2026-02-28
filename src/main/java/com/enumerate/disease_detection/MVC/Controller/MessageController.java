package com.enumerate.disease_detection.MVC.Controller;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.MVC.Mapper.ChatMessageMapper;
import com.enumerate.disease_detection.MVC.Mapper.SessionMapper;
import com.enumerate.disease_detection.MVC.POJO.DTO.AgentMessageDTO;
import com.enumerate.disease_detection.MVC.POJO.PO.ChatMessagePO;
import com.enumerate.disease_detection.MVC.POJO.PO.ChatSessionPO;

import com.enumerate.disease_detection.MVC.Service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/message")
@Slf4j
public class MessageController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private SessionMapper sessionMapper;

    @GetMapping
    @CrossOrigin
    public Result<List<ChatMessagePO>> getMessage(@RequestParam String sessionId) {
        List<ChatMessagePO> res = messageService.getMessage(sessionId);
        return Result.success(res);
    }

    @PostMapping("/agent")
    @CrossOrigin
    public Result<String> saveAgentMessage(@RequestBody AgentMessageDTO dto) {
        try {
            // 1. 保存用户消息
            ChatMessagePO userMsg = ChatMessagePO.builder().build();
            userMsg.setSessionId(dto.getSessionId());
            userMsg.setMessageContent(dto.getUserMessage());
            userMsg.setMessageRole("0"); // 用户消息
            userMsg.setMessageTime(LocalDateTime.now());
            chatMessageMapper.insert(userMsg);

            // 2. 保存机器人消息（包含agentData）
            ChatMessagePO robotMsg = ChatMessagePO.builder().build();
            robotMsg.setSessionId(dto.getSessionId());
            robotMsg.setMessageContent(dto.getRobotMessage().getFinalContent());
            robotMsg.setMessageRole("1"); // 机器人消息
            robotMsg.setMessageTime(LocalDateTime.now());

            // 将robotMessage对象转换为JSON字符串存储
            ObjectMapper mapper = new ObjectMapper();
            String agentDataJson = mapper.writeValueAsString(dto.getRobotMessage());
            robotMsg.setAgentData(agentDataJson);

            chatMessageMapper.insert(robotMsg);

            // 3. 更新会话的最后聊天时间
            ChatSessionPO session = sessionMapper.selectById(dto.getSessionId());
            if (session != null) {
                session.setLastChatTime(LocalDateTime.now());
                sessionMapper.updateById(session);
            }

            return Result.success();
        } catch (Exception e) {
            log.error("保存Agent消息失败", e);
            return Result.success("保存失败");
        }
    }
}
