package com.enumerate.disease_detection.MVC.Service.Impl;

import com.enumerate.disease_detection.MVC.Mapper.FarmMapper;
import com.enumerate.disease_detection.MVC.Mapper.PlotMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.FarmPO;
import com.enumerate.disease_detection.MVC.POJO.PO.PlotPO;
import com.enumerate.disease_detection.MVC.POJO.VO.AccumulatedTempPredictionVO;
import com.enumerate.disease_detection.MVC.POJO.VO.AccumulatedTempVO;
import com.enumerate.disease_detection.MVC.POJO.VO.DailyTempRecordVO;
import com.enumerate.disease_detection.MVC.Service.PlotAccumulatedTempService;
import com.enumerate.disease_detection.MVC.Service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PlotAccumulatedTempServiceImpl implements PlotAccumulatedTempService {

    @Autowired
    private PlotMapper plotMapper;

    @Autowired
    private FarmMapper farmMapper;

    @Autowired
    private WeatherService weatherService;

    @Override
    public AccumulatedTempVO getAccumulatedTemp(String plotId, String startDate, Double baseTemp) {
        PlotPO plot = plotMapper.selectById(plotId);
        if (plot == null) {
            throw new RuntimeException("地块不存在");
        }

        FarmPO farm = farmMapper.selectById(plot.getFarmId());
        String location = (farm != null && farm.getLocation() != null) ? farm.getLocation() : "未知位置";

        // 默认起点日期：播种日期 -> 今天的前一天
        String calcStartDate = (startDate != null && !startDate.isEmpty()) ? startDate : plot.getSowingDate();
        if (calcStartDate == null || calcStartDate.isEmpty()) {
            calcStartDate = LocalDate.now().minusDays(30).toString(); // 无播种日期则默认30天
        }
        
        String endDate = LocalDate.now().toString();

        List<DailyTempRecordVO> records = weatherService.getHistoricalWeather(location, calcStartDate, endDate, baseTemp);

        double currentTemp = 0;
        if (!records.isEmpty()) {
            currentTemp = records.get(records.size() - 1).getAccumulated();
        }

        // 目标积温常数：根据作物获取
        double targetTemp = getTargetTempForCrop(plot.getCropType());

        return AccumulatedTempVO.builder()
                .currentTemp(currentTemp)
                .targetTemp(targetTemp)
                .records(records)
                .build();
    }

    @Override
    public AccumulatedTempPredictionVO getAccumulatedTempPrediction(String plotId, Double baseTemp) {
        PlotPO plot = plotMapper.selectById(plotId);
        if (plot == null) {
            throw new RuntimeException("地块不存在");
        }

        FarmPO farm = farmMapper.selectById(plot.getFarmId());
        String location = (farm != null && farm.getLocation() != null) ? farm.getLocation() : "未知位置";

        // 获取当前统计作为预测起点
        AccumulatedTempVO currentStats = getAccumulatedTemp(plotId, null, baseTemp);
        double currentAccumulated = currentStats.getCurrentTemp();
        double targetTemp = currentStats.getTargetTemp();

        // 获取未来 15 天预报
        List<DailyTempRecordVO> forecast = weatherService.getWeatherForecast(location, 15);
        
        List<DailyTempRecordVO> predictionRecords = new ArrayList<>();
        double runningAccumulated = currentAccumulated;
        String nextStageDate = null;

        for (DailyTempRecordVO f : forecast) {
            double dailyEff = Math.max(0, f.getTemp() - baseTemp);
            runningAccumulated += dailyEff;
            
            predictionRecords.add(DailyTempRecordVO.builder()
                    .date(f.getDate())
                    .temp(f.getTemp())
                    .accumulated(Math.round(runningAccumulated * 10.0) / 10.0)
                    .build());

            // 预测下一物候期日期
            if (nextStageDate == null && runningAccumulated >= targetTemp) {
                nextStageDate = f.getDate();
            }
        }

        return AccumulatedTempPredictionVO.builder()
                .predictedNextStageDate(nextStageDate != null ? nextStageDate : "超出预报范围")
                .nextStageName(getNextStageName(plot.getGrowthStage()))
                .predictedMatureDate(calculatePredictedMatureDate(plot.getCropType(), plot.getSowingDate()))
                .predictionRecords(predictionRecords)
                .build();
    }

    /**
     * 根据作物类型获取当前阶段所需的目标积温 (模拟数据)
     */
    private double getTargetTempForCrop(String cropType) {
        if ("小麦".equals(cropType)) return 600.0;
        if ("玉米".equals(cropType)) return 1200.0;
        if ("水稻".equals(cropType)) return 1500.0;
        return 800.0; // 默认
    }

    /**
     * 根据当前生长阶段推断下一阶段 (模拟数据)
     */
    private String getNextStageName(String currentStage) {
        if (currentStage == null) return "发芽期";
        switch (currentStage) {
            case "播种期": return "出苗期";
            case "出苗期": return "分蘖期";
            case "分蘖期": return "拔节期";
            case "拔节期": return "抽穗期";
            case "抽穗期": return "成熟期";
            default: return "生长阶段";
        }
    }

    /**
     * 预测成熟日期 (简单模拟)
     */
    private String calculatePredictedMatureDate(String cropType, String sowingDate) {
        try {
            if (sowingDate == null) return LocalDate.now().plusMonths(3).toString();
            LocalDate sDate = LocalDate.parse(sowingDate);
            if ("小麦".equals(cropType)) return sDate.plusDays(240).toString();
            if ("玉米".equals(cropType)) return sDate.plusDays(120).toString();
            return sDate.plusDays(100).toString();
        } catch (Exception e) {
            return "未知";
        }
    }
}
