package com.enumerate.disease_detection.MVC.Service;

import com.enumerate.disease_detection.MVC.POJO.VO.AccumulatedTempPredictionVO;
import com.enumerate.disease_detection.MVC.POJO.VO.AccumulatedTempVO;

public interface PlotAccumulatedTempService {
    AccumulatedTempVO getAccumulatedTemp(String plotId, String startDate, Double baseTemp);
    AccumulatedTempPredictionVO getAccumulatedTempPrediction(String plotId, Double baseTemp);
}
