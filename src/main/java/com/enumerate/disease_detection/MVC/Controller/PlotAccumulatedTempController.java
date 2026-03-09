package com.enumerate.disease_detection.MVC.Controller;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.MVC.POJO.VO.AccumulatedTempPredictionVO;
import com.enumerate.disease_detection.MVC.POJO.VO.AccumulatedTempVO;
import com.enumerate.disease_detection.MVC.Service.PlotAccumulatedTempService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plots")
@CrossOrigin
@Slf4j
public class PlotAccumulatedTempController {

    @Autowired
    private PlotAccumulatedTempService plotAccumulatedTempService;

    @GetMapping("/{plotId}/accumulated-temp")
    public Result<AccumulatedTempVO> getAccumulatedTemp(
            @PathVariable String plotId,
            @RequestParam(required = false) String startDate,
            @RequestParam(defaultValue = "10.0") Double baseTemp) {
        log.info("获取地块积温统计数据：plotId={}, startDate={}, baseTemp={}", plotId, startDate, baseTemp);
        AccumulatedTempVO result = plotAccumulatedTempService.getAccumulatedTemp(plotId, startDate, baseTemp);
        return Result.success(result);
    }

    @GetMapping("/{plotId}/accumulated-temp/prediction")
    public Result<AccumulatedTempPredictionVO> getAccumulatedTempPrediction(
            @PathVariable String plotId,
            @RequestParam(defaultValue = "10.0") Double baseTemp) {
        log.info("获取地块积温预测数据：plotId={}, baseTemp={}", plotId, baseTemp);
        AccumulatedTempPredictionVO result = plotAccumulatedTempService.getAccumulatedTempPrediction(plotId, baseTemp);
        return Result.success(result);
    }
}
