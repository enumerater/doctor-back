package com.enumerate.disease_detection.POJO.DTO;


import lombok.Data;

@Data
public class AvatarDTO {
    // 前端返回的提示信息
    private String message;
    // 头像的实际URL（核心字段）
    private String url;
}