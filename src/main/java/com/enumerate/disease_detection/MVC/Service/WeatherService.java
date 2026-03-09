package com.enumerate.disease_detection.MVC.Service;

import com.enumerate.disease_detection.MVC.POJO.VO.DailyTempRecordVO;
import java.util.List;

public interface WeatherService {
    /**
     * 获取历史天气记录
     * @param location 地点或经纬度
     * @param startDate 起始日期 (YYYY-MM-DD)
     * @param endDate 结束日期 (YYYY-MM-DD)
     * @return 每日气温记录
     */
    List<DailyTempRecordVO> getHistoricalWeather(String location, String startDate, String endDate, Double baseTemp);

    /**
     * 获取天气预报
     * @param location 地点或经纬度
     * @param days 预报天数
     * @return 每日气温预报
     */
    List<DailyTempRecordVO> getWeatherForecast(String location, int days);
}
