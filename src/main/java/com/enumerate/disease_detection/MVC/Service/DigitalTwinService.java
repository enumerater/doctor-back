package com.enumerate.disease_detection.MVC.Service;

import com.enumerate.disease_detection.MVC.POJO.PO.PlotPO;
import com.enumerate.disease_detection.MVC.POJO.PO.SensorDataPO;

import java.util.List;
import java.util.Map;

public interface DigitalTwinService {
    List<PlotPO> getAllPlots();
    SensorDataPO getPlotDetail(Long plotId);
    Map<String, Object> getPlotHistory(Long plotId, String type, String range);
    void controlPlot(Long plotId, String action, Integer duration);
    
    // AI 模拟与计算
    void simulateDataUpdate();
}
