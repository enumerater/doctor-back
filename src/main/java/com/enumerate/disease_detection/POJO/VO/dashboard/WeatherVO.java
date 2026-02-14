package com.enumerate.disease_detection.POJO.VO.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherVO {
    private CurrentWeather current;
    private List<ForecastItem> forecast;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentWeather {
        private Double temp;
        private Integer humidity;
        private String wind;
        private String weather;
        private String icon;
        private Integer aqi;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastItem {
        private String date;
        private String weather;
        private String icon;
        private Double high;
        private Double low;
    }
}
