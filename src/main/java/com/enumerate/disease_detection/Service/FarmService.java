package com.enumerate.disease_detection.Service;

import com.enumerate.disease_detection.Local.UserContextHolder;
import com.enumerate.disease_detection.Mapper.FarmMapper;
import com.enumerate.disease_detection.Mapper.PlotMapper;
import com.enumerate.disease_detection.POJO.DTO.FarmDTO;
import com.enumerate.disease_detection.POJO.DTO.PlotDTO;
import com.enumerate.disease_detection.POJO.PO.FarmPO;
import com.enumerate.disease_detection.POJO.PO.PlotPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FarmService {

    @Autowired
    private FarmMapper farmMapper;

    public void add(FarmDTO farmDTO) {
        log.info("FarmService接收到的参数：【{}】", farmDTO);
        FarmPO farmPO = FarmPO.builder()
                .area(farmDTO.getArea())
                .location(farmDTO.getLocation())
                .name(farmDTO.getName())
                .userId(UserContextHolder.getUserId())
                .plotCount(0)
                .build();
        farmMapper.insert(farmPO);
    }

    public void update(String farmId, FarmDTO farmDTO) {
        log.info("FarmService接收到的参数：【{}】", farmDTO);
        FarmPO farmPO = FarmPO.builder()
                .id(Long.parseLong(farmId))
                .area(farmDTO.getArea())
                .location(farmDTO.getLocation())
                .name(farmDTO.getName())
                .userId(UserContextHolder.getUserId())
                .plotCount(0)
                .build();
        farmMapper.updateById(farmPO);
    }

    @Autowired
    private PlotMapper plotMapper;

    public void addPlot(String farmId, PlotDTO plotDTO) {
        log.info("FarmService接收到的参数：【{}】", farmId);
        FarmPO farmPO = farmMapper.selectById(farmId);
        farmPO.setPlotCount(farmPO.getPlotCount() + 1);
        farmMapper.updateById(farmPO);

        PlotPO p = PlotPO.builder()
                .farmId(Long.parseLong(farmId))
                .name(plotDTO.getName())
                .cropType(plotDTO.getCropType())
                .area(plotDTO.getArea())
                .sowingDate(plotDTO.getSowingDate())
                .soilType(plotDTO.getSoilType())

                .build();
        plotMapper.insert(p);
    }
}
