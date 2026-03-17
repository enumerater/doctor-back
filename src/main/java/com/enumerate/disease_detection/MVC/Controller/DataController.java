package com.enumerate.disease_detection.MVC.Controller;


import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.MVC.POJO.VO.Weather.WealthVO;
import com.enumerate.disease_detection.MVC.Service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping
@Slf4j
public class DataController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/data/getDayTemHum")
    public Result<WealthVO> getDayTemHum(@RequestParam("area") String location) {
        log.info("【数据服务】获取日温度和湿度数据");

        Map<String, Double> realWeather = weatherService.getCurrentTempAndHum(location);
        double baseTemp = realWeather.get("temp");
        double baseHum = realWeather.get("humidity");

        WealthVO wealthVO = new WealthVO();
        wealthVO.setTemperature(baseTemp);
        wealthVO.setHumidity(baseHum);

        return Result.success(wealthVO);
    }
}
