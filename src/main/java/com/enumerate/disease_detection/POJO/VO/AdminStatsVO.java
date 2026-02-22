package com.enumerate.disease_detection.POJO.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminStatsVO {
    private Long totalUsers;
    private Long activeToday;
    private Long totalDiagnoses;
    private Long diagnosesToday;
    private Integer avgAccuracy;
    private Long knowledgeEntries;
    private Long feedbackCount;
    private Long feedbackPending;
}
