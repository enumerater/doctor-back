package com.enumerate.disease_detection.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.Mapper.FarmMapper;
import com.enumerate.disease_detection.Mapper.PlotMapper;
import com.enumerate.disease_detection.Mapper.PlotStageMapper;
import com.enumerate.disease_detection.POJO.DTO.FarmDTO;
import com.enumerate.disease_detection.POJO.DTO.PlotDTO;
import com.enumerate.disease_detection.POJO.DTO.PlotStageDTO;
import com.enumerate.disease_detection.POJO.PO.FarmPO;
import com.enumerate.disease_detection.POJO.PO.PlotPO;
import com.enumerate.disease_detection.POJO.PO.PlotStagePO;
import com.enumerate.disease_detection.POJO.VO.FarmDetailVO;
import com.enumerate.disease_detection.POJO.VO.FarmVO;
import com.enumerate.disease_detection.POJO.VO.PlotVO;
import com.enumerate.disease_detection.Service.FarmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/farm")
@CrossOrigin
@Slf4j
public class FarmController {
    @Autowired
    private FarmMapper farmMapper;

    @Autowired
    private FarmService farmService;

    @GetMapping("/list")
    public Result<List<FarmVO>> list() {
        List<FarmPO> t = farmMapper.selectList(null);
        List<FarmVO> res = t.stream().map(q -> FarmVO.builder()
                .id(String.valueOf( q.getId()))
                .userId(String.valueOf(q.getUserId()))
                .name(q.getName())
                .location(q.getLocation())
                .area(q.getArea())
                .plotCount(String.valueOf(q.getPlotCount()))
                .build()).toList();

        return Result.success(res);
    }

    @PostMapping
    public Result<String> add(@RequestBody FarmDTO farmDTO) {
        farmService.add(farmDTO);
        return Result.success("添加成功");
    }

    @GetMapping("/{farmId}")
    public Result<FarmDetailVO> get(@PathVariable String farmId) {
        log.info("获取农场信息：【{}】", farmId);
        FarmPO farmPO = farmMapper.selectById(farmId);

        List<PlotPO> plotPOLi = plotMapper.selectList(new QueryWrapper<PlotPO>().eq("farm_id", farmId));
        List<PlotVO> plotVOList = plotPOLi.stream().map(q -> PlotVO.builder()
                .id(String.valueOf(q.getId()))
                .farmId(String.valueOf(q.getFarmId()))
                .name(q.getName())
                .cropType(q.getCropType())
                .area(q.getArea())
                .sowingDate(q.getSowingDate())
                .soilType(q.getSoilType())
                .growthStage(q.getGrowthStage())
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .build()).toList();

        FarmDetailVO farmDetailVO = FarmDetailVO.builder()
                .id(String.valueOf(farmPO.getId()))
                .userId(String.valueOf(farmPO.getUserId()))
                .name(farmPO.getName())
                .location(farmPO.getLocation())
                .area(farmPO.getArea())
                .plotCount(String.valueOf(farmPO.getPlotCount()))
                .plots(plotVOList)
                .build();

        return Result.success(farmDetailVO);
    }
    @PutMapping("/{farmId}")
    public Result<String> update(@PathVariable String farmId, @RequestBody FarmDTO farmDTO) {
        log.info("更新农场信息：【{}】", farmId);
        farmService.update(farmId, farmDTO);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{farmId}")
    public Result<String> delete(@PathVariable String farmId) {
        log.info("删除农场信息：【{}】", farmId);
        farmMapper.deleteById(farmId);
        return Result.success("删除成功");
    }

    @Autowired
    private PlotMapper plotMapper;

    @PostMapping("/{farmId}/plot")
    public Result<String> addPlot(@PathVariable String farmId,@RequestBody PlotDTO plotDTO) {
        log.info("添加地块：【{}】", farmId);
        farmService.addPlot(farmId,plotDTO);
        return Result.success("添加成功");
    }

    @GetMapping("/{farmId}/plot/{plotId}")
    public Result<PlotVO> getPlot(@PathVariable String farmId,@PathVariable String plotId) {
        log.info("获取地块信息：【{}】", plotId);
        QueryWrapper<PlotPO> queryWrapper = new QueryWrapper<PlotPO>().eq("id", plotId).eq("farm_id", farmId);
        PlotPO plotPO = plotMapper.selectOne(queryWrapper);

        PlotVO plotVO = PlotVO.builder().build();
        BeanUtils.copyProperties(plotPO, plotVO);

        QueryWrapper<PlotStagePO> queryWrapper2 = new QueryWrapper<PlotStagePO>().eq("plot_id", plotId);
        List<PlotStagePO> plotStagePOS = plotStageMapper.selectList(queryWrapper2);

        plotVO.setPlotStagePOList(plotStagePOS);

        return Result.success(plotVO);
    }

    @PutMapping("/{farmId}/plot/{plotId}")
    public Result<String> updatePlot(@PathVariable String farmId,@PathVariable String plotId,@RequestBody PlotDTO plotDTO) {
        log.info("更新地块信息：【{}】", plotId);
        QueryWrapper<PlotPO> queryWrapper = new QueryWrapper<PlotPO>().eq("id", plotId).eq("farm_id", farmId);
        PlotPO plotPO = plotMapper.selectOne(queryWrapper);
        plotPO.setName(plotDTO.getName());
        plotPO.setCropType(plotDTO.getCropType());
        plotPO.setArea(plotDTO.getArea());
        plotPO.setSowingDate(plotDTO.getSowingDate());
        plotMapper.updateById(plotPO);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{farmId}/plot/{plotId}")
    public Result<String> deletePlot(@PathVariable String farmId,@PathVariable String plotId) {
        log.info("删除地块信息：【{}】", plotId);
        QueryWrapper<PlotPO> queryWrapper = new QueryWrapper<PlotPO>().eq("id", plotId).eq("farm_id", farmId);
        PlotPO plotPO = plotMapper.selectOne(queryWrapper);
        plotMapper.deleteById(plotPO);
        return Result.success("删除成功");
    }

    @Autowired
    private PlotStageMapper plotStageMapper;

    @PutMapping("/{farmId}/plot/{plotId}/stage")
    public Result<String> updatePlotStage(@PathVariable String farmId,@PathVariable String plotId,@RequestBody PlotStageDTO plotStageDTO) {
        log.info("更新地块信息：【{}】", plotId);
        QueryWrapper<PlotPO> queryWrapper = new QueryWrapper<PlotPO>().eq("id", plotId).eq("farm_id", farmId);
        PlotPO plotPO = plotMapper.selectOne(queryWrapper);
        plotPO.setGrowthStage(plotStageDTO.getStage());
        plotMapper.updateById(plotPO);

        PlotStagePO plotStagePO = PlotStagePO.builder()
                .plotId(plotId)
                .stage(plotStageDTO.getStage())
                .date(plotStageDTO.getDate())
                .build();

        plotStageMapper.insert(plotStagePO);


        return Result.success("更新成功");
    }





}
