package com.enumerate.disease_detection.MVC.POJO.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserVO {
    private String id;
    private String username;
    private String role;
    private String status;
    private String createdAt;
    private Long diagnosisCount;
    private String lastLogin;
}
