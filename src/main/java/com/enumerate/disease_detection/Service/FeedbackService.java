package com.enumerate.disease_detection.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.Local.UserContextHolder;
import com.enumerate.disease_detection.Mapper.FeedbackMapper;
import com.enumerate.disease_detection.POJO.DTO.FeedbackDTO;
import com.enumerate.disease_detection.POJO.PO.FeedbackPO;
import com.enumerate.disease_detection.POJO.VO.FeedbackStatsVO;
import com.enumerate.disease_detection.POJO.VO.FeedbackVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    public FeedbackPO submit(FeedbackDTO dto) {
        FeedbackPO po = FeedbackPO.builder()
                .userId(UserContextHolder.getUserId())
                .diagnosisId(dto.getDiagnosisId())
                .accuracy(dto.getAccuracy())
                .correctDisease(dto.getCorrectDisease() != null ? dto.getCorrectDisease() : "")
                .rating(dto.getRating())
                .comment(dto.getComment())
                .cropType(dto.getCropType())
                .diagnosedDisease(dto.getDiagnosedDisease())
                .status("pending")
                .build();
        feedbackMapper.insert(po);
        return po;
    }

    public FeedbackPO getByDiagnosisId(String diagnosisId) {
        return feedbackMapper.selectOne(
                new QueryWrapper<FeedbackPO>().eq("diagnosis_id", diagnosisId)
        );
    }

    public List<FeedbackVO> list(String status, String accuracy) {
        return feedbackMapper.selectFeedbackList(status, accuracy);
    }

    public FeedbackPO updateStatus(Long id, String status) {
        FeedbackPO po = feedbackMapper.selectById(id);
        po.setStatus(status);
        feedbackMapper.updateById(po);
        return po;
    }

    public FeedbackStatsVO stats() {
        Map<String, Object> map = feedbackMapper.selectFeedbackStats();

        long total = map.get("total") != null ? ((Number) map.get("total")).longValue() : 0;
        long correct = map.get("correct") != null ? ((Number) map.get("correct")).longValue() : 0;
        long partial = map.get("partial") != null ? ((Number) map.get("partial")).longValue() : 0;
        long incorrect = map.get("incorrect") != null ? ((Number) map.get("incorrect")).longValue() : 0;

        int accuracyRate = 0;
        String avgRating = "0.0";

        if (total > 0) {
            accuracyRate = (int) ((correct + partial * 0.5) / total * 100);
            if (map.get("avgRating") != null) {
                BigDecimal avg = new BigDecimal(map.get("avgRating").toString());
                avgRating = avg.setScale(1, RoundingMode.HALF_UP).toString();
            }
        }

        return FeedbackStatsVO.builder()
                .total(total)
                .correct(correct)
                .partial(partial)
                .incorrect(incorrect)
                .accuracyRate(accuracyRate)
                .avgRating(avgRating)
                .build();
    }
}
