package com.enumerate.disease_detection.MVC.POJO.VO.Weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoResponse {
    private String code;          // 状态码（200为成功）
    private List<Location> location; // 地理位置列表
    private Refer refer;          // 参考信息



    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        private String name;      // 地区名称
        private String id;        // LocationID
        private String lat;       // 纬度
        private String lon;       // 经度
        private String type;      // 类型（city/district）
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Refer {
        private List<String> sources; // 数据来源
        private List<String> license; // 许可证
    }
}