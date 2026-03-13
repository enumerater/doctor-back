package com.enumerate.disease_detection.MVC.POJO.VO.Weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Weather7dResponse {
    private String code;          // 状态码
    private String updateTime;    // 更新时间
    private List<Daily> daily;    // 每日预报列表
    private Refer refer;          // 参考信息

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Daily {
        private String fxDate;     // 预报日期（yyyy-MM-dd）
        private String tempMax;    // 最高温
        private String tempMin;    // 最低温
        private String tempAvg;    // 平均温（部分版本返回）
        private String textDay;    // 白天天气状况
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Refer {
        private List<String> sources;
        private List<String> license;
    }
}
