package com.enumerate.disease_detection.MVC.POJO.VO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLoginVO {
    private String token;
    private String id;
    private String username;
    private String msg;
    private String sessionId;
    private Boolean hasPassword;
    private String role;

    private String email;
    private String avatar;

    private String status;
}
