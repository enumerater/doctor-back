package com.enumerate.disease_detection.MVC.Service.Impl;

import com.enumerate.disease_detection.MVC.POJO.VO.DailyTempRecordVO;
import com.enumerate.disease_detection.MVC.Service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    @Override
    public List<DailyTempRecordVO> getHistoricalWeather(String location, String startDate, String endDate, Double baseTemp) {
        log.info("获取历史天气：location={}, startDate={}, endDate={}, baseTemp={}", location, startDate, endDate, baseTemp);
        
        // 实际开发中应调用第三方 API (如和风天气、OpenWeather)
        // 此处返回模拟数据以驱动前端界面
        List<DailyTempRecordVO> records = new ArrayList<>();
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            Random random = new Random(location.hashCode() + start.toEpochDay());
            double currentAccumulated = 0;
            
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                // 模拟平均气温 10°C - 25°C 之间的波动
                double avgTemp = 12.0 + random.nextDouble() * 10.0;
                // 有效积温 = max(0, 日均温 - 生物学下限温度)
                double dailyEff = Math.max(0, avgTemp - baseTemp);
                currentAccumulated += dailyEff;
                
                records.add(DailyTempRecordVO.builder()
                        .date(date.toString())
                        .temp(Math.round(avgTemp * 10.0) / 10.0)
                        .accumulated(Math.round(currentAccumulated * 10.0) / 10.0)
                        .build());
            }
        } catch (Exception e) {
            log.error("生成模拟历史天气失败", e);
        }
        return records;
    }

    @Override
    public List<DailyTempRecordVO> getWeatherForecast(String location, int days) {
        log.info("获取天气预报：location={}, days={}", location, days);
        
        List<DailyTempRecordVO> records = new ArrayList<>();
        LocalDate today = LocalDate.now();
        Random random = new Random(location.hashCode() + today.toEpochDay());
        
        for (int i = 0; i < days; i++) {
            LocalDate date = today.plusDays(i);
            // 模拟预报气温稍高于当前平均
            double avgTemp = 15.0 + random.nextDouble() * 12.0;
            records.add(DailyTempRecordVO.builder()
                    .date(date.toString())
                    .temp(Math.round(avgTemp * 10.0) / 10.0)
                    .build());
        }
        return records;
    }
}
