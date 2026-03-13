package com.enumerate.disease_detection.MVC.Controller;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.MVC.POJO.PO.PlotPO;
import com.enumerate.disease_detection.MVC.POJO.PO.SensorDataPO;
import com.enumerate.disease_detection.MVC.Service.DigitalTwinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/twin")
@CrossOrigin
@Slf4j
public class DigitalTwinController {

    @Autowired
    private DigitalTwinService digitalTwinService;

    @GetMapping("/plots")
    public Result<List<PlotPO>> getAllPlots() {
        return Result.success(digitalTwinService.getAllPlots());
    }

    @GetMapping("/plots/{id}/detail")
    public Result<SensorDataPO> getPlotDetail(@PathVariable Long id) {
        return Result.success(digitalTwinService.getPlotDetail(id));
    }

    @GetMapping("/plots/{id}/history")
    public Result<Map<String, Object>> getPlotHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "temp") String type,
            @RequestParam(defaultValue = "24h") String range) {
        return Result.success(digitalTwinService.getPlotHistory(id, type, range));
    }

    @PostMapping("/control")
    public Result<String> controlPlot(@RequestBody Map<String, Object> params) {
        Long plotId = Long.valueOf(params.get("plot_id").toString());
        String action = params.get("action").toString();
        Integer duration = (Integer) params.get("duration");
        digitalTwinService.controlPlot(plotId, action, duration);
        return Result.success("指令已下发");
    }

    @PostMapping("/simulate/trigger")
    public Result<String> triggerSimulation() {
        digitalTwinService.simulateDataUpdate();
        return Result.success("模拟更新已触发");
    }
}
