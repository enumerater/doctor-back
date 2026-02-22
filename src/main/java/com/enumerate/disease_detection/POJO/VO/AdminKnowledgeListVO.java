package com.enumerate.disease_detection.POJO.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminKnowledgeListVO {
    private List<AdminKnowledgeVO> list;
    private Long total;
}
