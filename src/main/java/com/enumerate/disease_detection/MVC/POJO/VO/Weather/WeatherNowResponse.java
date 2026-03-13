package com.enumerate.disease_detection.MVC.POJO.VO.Weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherNowResponse {
    private String code;          // 状态码
    private String updateTime;    // 更新时间
    private Now now;              // 实时天气数据
    private Refer refer;          // 参考信息

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Now {
        private String obsTime;    // 观测时间
        private String temp;       // 实时气温（℃）
        private String feelsLike;  // 体感温度
        private String text;       // 天气状况文字
        private String humidity;   // 相对湿度（%）
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Refer {
        private List<String> sources;
        private List<String> license;
    }
}