package com.enumerate.disease_detection.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.Mapper.DiseasesMapper;
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
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String cropName,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        DiseasesPageResult res = knowledgeService.getDiseasesByCrop(category, cropName, keyword, page, pageSize);

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
    public Result<DiseasesPageResult> searchDisease(@RequestParam String keyword,
                                                  @RequestParam Integer page, @RequestParam Integer pageSize) {
        log.info("=== searchDisease ===");
        DiseasesPageResult res = knowledgeService.getDiseasesByCrop("", "", keyword, page, pageSize);
        return Result.success( res);
    }

    @GetMapping("/diseases/categories")
    public Result<List<String>> getDiseaseCategories() {
        log.info("=== getDiseaseCategories ===");

        List<String> res = diseasesMapper.selectList(new QueryWrapper<DiseasesPO>().select("category")).stream().map(DiseasesPO::getCategory).distinct().toList();

        return Result.success(res);
    }


    @GetMapping("/diseases/crop_names")
    public Result<List<String>> getCropName(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String crop_name,
            @RequestParam(required = false) String keyword) {
        List<String> res = knowledgeService.getCropName(category);

        return Result.success(res);
    }


}
