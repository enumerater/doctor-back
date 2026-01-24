package com.enumerate.disease_detection.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.Mapper.SessionMapper;
import com.enumerate.disease_detection.POJO.DTO.ChatSessionDTO;
import com.enumerate.disease_detection.POJO.PO.ChatSessionPO;
import com.enumerate.disease_detection.POJO.VO.SessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/session")
public class SessionController {
    @Autowired
    private SessionMapper sessionMapper;

    @GetMapping
    @CrossOrigin
    public Result<SessionVO> getSession(@RequestParam Long id) {
        SessionVO sessionVO = new SessionVO();
        ChatSessionPO chatSessionPO = sessionMapper.selectById(id);
        sessionVO.setId(chatSessionPO.getId());
        sessionVO.setSessionTitle(chatSessionPO.getSessionTitle());
        sessionVO.setLastChatTime(chatSessionPO.getLastChatTime().toString());
        sessionVO.setSessionStatus(chatSessionPO.getSessionStatus());

        return Result.success(sessionVO);
    }

    @GetMapping("/page")
    @CrossOrigin
    public Result<List<SessionVO>> getAllSession() {
        // 按时间排序
        List<ChatSessionPO> li = sessionMapper.selectList(null, new QueryWrapper<ChatSessionPO>().orderByDesc("create_time"));

        List<SessionVO> sessionVOList = new ArrayList<>();
        for (ChatSessionPO chatSessionPO : li) {
            SessionVO sessionVO = new SessionVO();
            sessionVO.setId(chatSessionPO.getId());
            sessionVO.setSessionTitle(chatSessionPO.getSessionTitle());
            sessionVO.setLastChatTime(chatSessionPO.getLastChatTime().toString());
            sessionVO.setSessionStatus(chatSessionPO.getSessionStatus());
            sessionVO.setSessionId(chatSessionPO.getSessionId());
            sessionVOList.add(sessionVO);
        }

        return Result.success(sessionVOList);
    }

    @PostMapping
    @CrossOrigin
    public Result<String> createSession(@RequestBody ChatSessionDTO chatSessionDTO) {
        ChatSessionPO chatSessionPO = ChatSessionPO.builder().build();
        chatSessionPO.setUserId(chatSessionDTO.getUserId());
        chatSessionPO.setSessionTitle(chatSessionDTO.getSessionTitle());
        chatSessionPO.setSessionStatus("1");
        chatSessionPO.setLastChatTime(LocalDateTime.now());
        chatSessionPO.setDeleted("0");

        chatSessionPO.setSessionId(chatSessionDTO.getSessionId());

        sessionMapper.insert(chatSessionPO);
        return Result.success("创建成功");
    }

    @PutMapping
    @CrossOrigin
    public Result<String> updateSession(@RequestBody ChatSessionDTO chatSessionDTO) {
        ChatSessionPO chatSessionPO = ChatSessionPO.builder().build();
        chatSessionPO.setSessionTitle(chatSessionDTO.getSessionTitle());
        chatSessionPO.setLastChatTime(LocalDateTime.now());
        sessionMapper.updateById(chatSessionPO);
        return Result.success("更新成功");
    }

    @DeleteMapping
    @CrossOrigin
    public Result<String> deleteSession(@RequestParam Long id) {
        sessionMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
