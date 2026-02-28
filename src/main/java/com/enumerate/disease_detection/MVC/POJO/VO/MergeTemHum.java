package com.enumerate.disease_detection.MVC.POJO.VO;

import lombok.Data;

import java.util.List;

@Data
public class MergeTemHum {
    private List<DayTemperatureVO> temperature;
    private List<DayHumidityVO> humidity;
}
