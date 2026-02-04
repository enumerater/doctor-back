package com.enumerate.disease_detection.POJO.VO;

import lombok.Data;

import java.util.List;

@Data
public class MergeTemHum {
    private List<DayTemperatureVO> temperature;
    private List<DayHumidityVO> humidity;
}
