package com.enumerate.disease_detection.MVC.POJO.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackStatsVO {
    private Long total;
    private Long correct;
    private Long partial;
    private Long incorrect;
    private Integer accuracyRate;
    private String avgRating;
}
