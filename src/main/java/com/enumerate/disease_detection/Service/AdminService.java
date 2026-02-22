package com.enumerate.disease_detection.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.Mapper.AdminMapper;
import com.enumerate.disease_detection.Mapper.FeedbackMapper;
import com.enumerate.disease_detection.Mapper.KnowledgeMapper;
import com.enumerate.disease_detection.Mapper.UserMapper;
import com.enumerate.disease_detection.POJO.DTO.KnowledgeDTO;
import com.enumerate.disease_detection.POJO.PO.KnowledgePO;
import com.enumerate.disease_detection.POJO.PO.UserPO;
import com.enumerate.disease_detection.POJO.VO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private KnowledgeMapper knowledgeMapper;

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
        if ("1".equals(user.getStatus())) {
            user.setStatus("0");
        } else {
            user.setStatus("1");
        }
        userMapper.updateById(user);
        return user;
    }

    // Knowledge CRUD
    public AdminKnowledgeListVO getKnowledgeList(String keyword) {
        QueryWrapper<KnowledgePO> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.like("name", keyword).or().like("crop", keyword);
        }
        qw.orderByDesc("updated_at");

        List<KnowledgePO> poList = knowledgeMapper.selectList(qw);
        List<AdminKnowledgeVO> voList = new ArrayList<>();
        for (KnowledgePO po : poList) {
            voList.add(AdminKnowledgeVO.builder()
                    .id(String.valueOf(po.getId()))
                    .name(po.getName())
                    .crop(po.getCrop())
                    .category(po.getCategory())
                    .symptoms(po.getSymptoms())
                    .treatment(po.getTreatment())
                    .status(po.getStatus())
                    .updatedAt(po.getUpdatedAt())
                    .build());
        }

        return AdminKnowledgeListVO.builder()
                .list(voList)
                .total((long) voList.size())
                .build();
    }

    public KnowledgePO createKnowledge(KnowledgeDTO dto) {
        KnowledgePO po = KnowledgePO.builder()
                .name(dto.getName())
                .crop(dto.getCrop())
                .category(dto.getCategory())
                .symptoms(dto.getSymptoms())
                .treatment(dto.getTreatment())
                .status("draft")
                .build();
        knowledgeMapper.insert(po);
        return po;
    }

    public KnowledgePO updateKnowledge(Long id, KnowledgeDTO dto) {
        KnowledgePO po = knowledgeMapper.selectById(id);
        po.setName(dto.getName());
        po.setCrop(dto.getCrop());
        po.setCategory(dto.getCategory());
        po.setSymptoms(dto.getSymptoms());
        po.setTreatment(dto.getTreatment());
        knowledgeMapper.updateById(po);
        return knowledgeMapper.selectById(id);
    }

    public boolean deleteKnowledge(Long id) {
        knowledgeMapper.deleteById(id);
        return true;
    }
}
