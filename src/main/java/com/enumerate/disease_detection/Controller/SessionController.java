package com.enumerate.disease_detection.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.Local.UserContextHolder;
import com.enumerate.disease_detection.Mapper.SessionMapper;
import com.enumerate.disease_detection.POJO.DTO.ChatSessionDTO;

import com.enumerate.disease_detection.POJO.PO.ChatSessionPO;
import com.enumerate.disease_detection.POJO.VO.SessionVO;
import com.enumerate.disease_detection.Tools.TitleTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/session")
@Slf4j
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
        // 按时间排序，只查询当前用户的会话
        Long userId = UserContextHolder.getUserId();
        List<ChatSessionPO> li = sessionMapper.selectList(null, new QueryWrapper<ChatSessionPO>()
                .eq("user_id", userId)
                .orderByDesc("create_time"));

        List<SessionVO> sessionVOList = new ArrayList<>();
        for (ChatSessionPO chatSessionPO : li) {
            SessionVO sessionVO = new SessionVO();
            sessionVO.setId(chatSessionPO.getId());
            sessionVO.setSessionTitle(chatSessionPO.getSessionTitle());
            sessionVO.setLastChatTime(chatSessionPO.getLastChatTime().toString());
            sessionVO.setSessionStatus(chatSessionPO.getSessionStatus());
            sessionVO.setSessionId(chatSessionPO.getSessionId());
            sessionVO.setSessionType(chatSessionPO.getSessionType());
            sessionVOList.add(sessionVO);
        }

        return Result.success(sessionVOList);
    }

    @PostMapping
    @CrossOrigin
    public Result<String> createSession(@RequestBody ChatSessionDTO chatSessionDTO) {
        ChatSessionPO chatSessionPO = ChatSessionPO.builder().build();
        chatSessionPO.setUserId(UserContextHolder.getUserId());
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

    @Autowired
    private TitleTool titleTool;

    @PutMapping("/title")
    @CrossOrigin
    public Result<String> updateSessionTitle(@RequestParam("title") String title, @RequestParam("sessionId") int sessionId) {
        String sessionTitle = titleTool.summarizeConversationTopic(title);
        // 1. 先定义要修改的字段和值（推荐用UpdateWrapper的set，避免实体空值问题）
        UpdateWrapper<ChatSessionPO> updateWrapper = new UpdateWrapper<>();
        // 条件：session_id = 拼接后的值（先拼接成变量，方便调试）
        String targetSessionId = UserContextHolder.getUserId() + String.valueOf(sessionId);
        updateWrapper.eq("session_id", targetSessionId);
        // 设置要修改的字段（直接在Wrapper中set，更稳妥）
        updateWrapper.set("session_title", sessionTitle);

        // 2. 执行更新（第一个参数传null，所有修改字段都在Wrapper中定义）
        sessionMapper.update(null, updateWrapper);
        return Result.success("更新成功");
    }

}
