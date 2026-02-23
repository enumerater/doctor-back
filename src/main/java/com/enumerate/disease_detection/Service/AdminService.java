package com.enumerate.disease_detection.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.Mapper.AdminMapper;
import com.enumerate.disease_detection.Mapper.FeedbackMapper;
import com.enumerate.disease_detection.Mapper.UserMapper;
import com.enumerate.disease_detection.POJO.PO.UserPO;
import com.enumerate.disease_detection.POJO.VO.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private UserMapper userMapper;


    public AdminStatsVO getStats() {
        Long totalUsers = adminMapper.countTotalUsers();
        Long activeToday = adminMapper.countActiveToday();
        Long totalDiagnoses = adminMapper.countTotalDiagnoses();
        Long diagnosesToday = adminMapper.countDiagnosesToday();
        Long knowledgeEntries = adminMapper.countKnowledgeEntries();
        Long feedbackCount = adminMapper.countFeedback();
        Long feedbackPending = adminMapper.countFeedbackPending();

        // Calculate avgAccuracy from feedback stats
        int avgAccuracy = 0;
        Map<String, Object> fbStats = feedbackMapper.selectFeedbackStats();
        if (fbStats != null && fbStats.get("total") != null) {
            long total = ((Number) fbStats.get("total")).longValue();
            if (total > 0) {
                long correct = fbStats.get("correct") != null ? ((Number) fbStats.get("correct")).longValue() : 0;
                long partial = fbStats.get("partial") != null ? ((Number) fbStats.get("partial")).longValue() : 0;
                avgAccuracy = (int) ((correct + partial * 0.5) / total * 100);
            }
        }

        return AdminStatsVO.builder()
                .totalUsers(totalUsers)
                .activeToday(activeToday)
                .totalDiagnoses(totalDiagnoses)
                .diagnosesToday(diagnosesToday)
                .avgAccuracy(avgAccuracy)
                .knowledgeEntries(knowledgeEntries)
                .feedbackCount(feedbackCount)
                .feedbackPending(feedbackPending)
                .build();
    }

    public AdminUserListVO getUserList(String keyword, String status, String role) {
        List<AdminUserVO> list = adminMapper.selectUserList(keyword, status, role);
        Long total = adminMapper.countUserList(keyword, status, role);
        return AdminUserListVO.builder()
                .list(list)
                .total(total)
                .build();
    }

    public UserPO toggleUserStatus(Long id) {
        UserPO user = userMapper.selectById(id);
        log.info("toggleUserStatus: {}", user);
        user.setStatus(user.getStatus().equals("1") ? "0" : "1");
        userMapper.updateById(user);
        return user;
    }
}
