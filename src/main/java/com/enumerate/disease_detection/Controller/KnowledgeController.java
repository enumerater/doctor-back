package com.enumerate.disease_detection.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.Mapper.DiseasesMapper;
import com.enumerate.disease_detection.Mapper.SeasonalMapper;
import com.enumerate.disease_detection.POJO.PO.DiseaseSeasonPO;
import com.enumerate.disease_detection.POJO.PO.DiseasesPO;
import com.enumerate.disease_detection.POJO.VO.CropListVO;
import com.enumerate.disease_detection.POJO.VO.DiseasesPageResult;
import com.enumerate.disease_detection.Service.KnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/knowledge")
@Slf4j
@CrossOrigin
public class KnowledgeController {

    @Autowired
    private KnowledgeService knowledgeService;

    @GetMapping("/crops")
    public Result<List<CropListVO>> getCrops() {
        log.info("=== getCrops ===");

        List<CropListVO> res = knowledgeService.getCrops();
        return Result.success(res);

    }

    @GetMapping("/diseases")
    public Result<DiseasesPageResult> getDiseasesByCrop(
            @RequestParam String cropName,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        DiseasesPageResult res = knowledgeService.getDiseasesByCrop(cropName, page, pageSize);

        return Result.success(res);
    }

    @Autowired
    private DiseasesMapper diseasesMapper;

    @GetMapping("/disease/{diseaseId}")
    public Result<DiseasesPO> getDiseaseById(@PathVariable String diseaseId) {
        log.info("=== getDiseaseById ===");

        DiseasesPO res = diseasesMapper.selectOne(new QueryWrapper<DiseasesPO>().eq("id", diseaseId));

        return Result.success(res);
    }

    @GetMapping("/search")
    public Result<List<DiseasesPO>> searchDisease(@RequestParam String keyword) {
        log.info("=== searchDisease ===");

        List<DiseasesPO> res = diseasesMapper.selectList(new QueryWrapper<DiseasesPO>().like("name", keyword));

        return Result.success( res);
    }

    @Autowired
    private SeasonalMapper seasonalMapper;

    @GetMapping("/seasonal-risks")
    public Result<List<DiseaseSeasonPO>> getSeasonalRisk(@RequestParam String month) {
        log.info("=== getSeasonalRisk ===");

        List<DiseaseSeasonPO> res = seasonalMapper.selectList(new QueryWrapper<DiseaseSeasonPO>().eq("month", month));

        return Result.success(res);
    }


}
