package com.enumerate.disease_detection.POJO.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminKnowledgeVO {
    private String id;
    private String name;
    private String crop;
    private String category;
    private String symptoms;
    private String treatment;
    private String status;
    private String updatedAt;
}
