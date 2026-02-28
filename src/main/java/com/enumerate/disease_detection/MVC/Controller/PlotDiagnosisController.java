package com.enumerate.disease_detection.MVC.Controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.MVC.Mapper.PlotDiagnosisMapper;
import com.enumerate.disease_detection.MVC.POJO.DTO.PlotDiagnosisDTO;
import com.enumerate.disease_detection.MVC.POJO.PO.PlotDiagnosisPO;
import com.enumerate.disease_detection.MVC.POJO.PO.PlotPO;
import com.enumerate.disease_detection.MVC.POJO.VO.PlotDiagnosisListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/plot_diagnosis")
@CrossOrigin
@Slf4j
public class PlotDiagnosisController {

    @Autowired
    private PlotDiagnosisMapper plotDiagnosisMapper;

    @GetMapping("/list")
    public Result<List<PlotDiagnosisListVO>> list(@RequestParam String plotId) {
        log.info("list");
        List<PlotDiagnosisPO> res = plotDiagnosisMapper.selectList( new QueryWrapper<PlotDiagnosisPO>().eq("plot_id",plotId));

        List<PlotDiagnosisListVO> list = new ArrayList<>() ;

        for (PlotDiagnosisPO po : res){
            PlotDiagnosisListVO t = PlotDiagnosisListVO.builder()
                    .id(po.getId())
                    .type(po.getType())
                    .targetId(po.getTargetId())
                    .title(po.getTitle())
                    .content(po.getContent())
                    .createdAt(po.getCreatedAt())
                    .build();

            list.add(t);
        }

        return Result.success(list);
    }

    @PostMapping("bind")
    public Result<String> bind(@RequestBody PlotDiagnosisDTO dto) {
        log.info("bind: {}", dto);
        PlotDiagnosisPO po = PlotDiagnosisPO.builder()
                .type(dto.getType())
                .targetId(dto.getTargetId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .plotId(dto.getPlotId())
                .build();
        plotDiagnosisMapper.insert(po);

        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        log.info("delete: {}", id);
        plotDiagnosisMapper.deleteById(id);
        return Result.success();
    }

}
