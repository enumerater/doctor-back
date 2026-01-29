package com.enumerate.disease_detection.ModelInterfaces;

import lombok.Data;

@Data
public class DiseaseSolution {
    // 植保用药方案
    private String pesticideScheme;
    // 田间管理方案（浇水/环境/栽培）
    private String fieldManageScheme;
    // 安全注意事项
    private String safeNoticeScheme;

    // 全参构造器（并行合并结果时直接调用）
    public DiseaseSolution(String pesticideScheme, String fieldManageScheme, String safeNoticeScheme) {
        this.pesticideScheme = pesticideScheme;
        this.fieldManageScheme = fieldManageScheme;
        this.safeNoticeScheme = safeNoticeScheme;
    }

}