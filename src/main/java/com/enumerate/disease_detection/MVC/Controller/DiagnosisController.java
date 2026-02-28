package com.enumerate.disease_detection.MVC.Controller;

import com.alibaba.fastjson2.JSONObject;
import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.Local.UserContextHolder;
import com.enumerate.disease_detection.MVC.Mapper.DiagnosisMapper;
import com.enumerate.disease_detection.MVC.POJO.DTO.DiagnosisDTO;
import com.enumerate.disease_detection.MVC.POJO.DTO.DiagnosisPutDTO;
import com.enumerate.disease_detection.MVC.POJO.PO.DiagnosisPO;
import com.enumerate.disease_detection.MVC.POJO.VO.DiagnosisStatusVO;
import com.enumerate.disease_detection.MVC.POJO.VO.DiagnosisVO;

import com.enumerate.disease_detection.MVC.POJO.VO.PageData;
import com.enumerate.disease_detection.MVC.Service.DiagnosisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/diagnosis")
@Slf4j
@CrossOrigin
public class DiagnosisController {

    @Autowired
    private DiagnosisMapper diagnosisMapper;

    @Autowired
    private DiagnosisService diagnosisService;

    @GetMapping("/list")
    public Result<PageData<DiagnosisVO>> list(@RequestParam(required = false) Integer page,
                                          @RequestParam(required = false) Integer pageSize,
                                          @RequestParam(required = false) String cropType,
                                          @RequestParam(required = false) String resultType
                                          ) {
        log.info("list");
        PageData<DiagnosisVO> res = diagnosisService.list(page, pageSize, cropType, resultType);
        return Result.success(res);
    }

    @GetMapping("/{id}")
    public Result<DiagnosisVO> getById(@PathVariable String id) {
        log.info("getById{}",id);
        DiagnosisPO res = diagnosisMapper.selectById(id);
        DiagnosisVO diagnosisVO = DiagnosisVO.builder()
                .id(id)
                .imageUrl(res.getImageUrl())
                .cropType(res.getCropType())
                .hasDisease(res.getHasDisease())
                .diseaseName(res.getDiseaseName())
                .severity(res.getSeverity())
                .result(res.getResult())
                .status(res.getStatus())
                .createdAt(res.getCreatedAt())
                .elapsedTime(res.getElapsedTime().toString())
                .plotId(res.getPlotId())
                .farmId(res.getFarmId())
                .notes(res.getNotes())
                .feedback(res.getFeedback())
                .build();
        return Result.success(diagnosisVO);
    }

    @PostMapping
    public Result<DiagnosisVO> save(@RequestBody DiagnosisDTO diagnosisDTO) {
        log.info("开始保存诊断记录");
        String resultJson = diagnosisDTO.getResult();

        // 1. 初始化hasDisease变量（提升作用域）
        Integer hasDisease = 0;

        try {
            // 2. 解析JSON字符串，获取type字段
            JSONObject resultObj = JSONObject.parseObject(resultJson);
            String resultType = resultObj.getString("type");

            // 3. 根据type值设置hasDisease
            if ("不健康作物".equals(resultType)) {
                hasDisease = 1;
            } else if ("非作物".equals(resultType)) {
                hasDisease = 2;
            } else {
                // 健康作物默认0
                hasDisease = 0;
            }
        } catch (Exception e) {
            log.error("解析result JSON字符串失败：{}", resultJson, e);
            // 解析失败时默认设为0，也可以根据业务抛异常
            hasDisease = 0;
        }

        // 4. 构建PO对象并保存
        DiagnosisPO diagnosisPO = DiagnosisPO.builder()
                .userId(UserContextHolder.getUserId())
                .imageUrl(diagnosisDTO.getImageUrl())
                .cropType(diagnosisDTO.getCropType())
                .hasDisease(hasDisease)  // 现在可以正常访问该变量
                .diseaseName(diagnosisDTO.getDiseaseName())
                .severity(diagnosisDTO.getSeverity())
                .result(diagnosisDTO.getResult())
                .status(diagnosisDTO.getStatus())
                .elapsedTime(diagnosisDTO.getElapsedTime())
                .build();

        diagnosisMapper.insert(diagnosisPO);
        log.info("诊断记录保存成功，ID：{}", diagnosisPO.getId());

        DiagnosisVO diagnosisVO = DiagnosisVO.builder()
                .id(String.valueOf(diagnosisPO.getId()))
                .imageUrl(diagnosisPO.getImageUrl())
                .cropType(diagnosisPO.getCropType())
                .hasDisease(diagnosisPO.getHasDisease())
                .diseaseName(diagnosisPO.getDiseaseName())
                .severity(diagnosisPO.getSeverity())
                .result(diagnosisPO.getResult())
                .status(diagnosisPO.getStatus())
                .createdAt(diagnosisPO.getCreatedAt())
                .elapsedTime(diagnosisPO.getElapsedTime().toString())
                .plotId(diagnosisPO.getPlotId())
                .farmId(diagnosisPO.getFarmId())
                .notes(diagnosisPO.getNotes())
                .feedback(diagnosisPO.getFeedback())
                .build();


        return Result.success(diagnosisVO);
    }

    @PutMapping("/{id}")
    public Result<DiagnosisPO> update(@PathVariable String id, @RequestBody DiagnosisPutDTO diagnosisPutDTO) {
        log.info("update {} {} ", id ,diagnosisPutDTO);
        DiagnosisPO diagnosisPO = diagnosisMapper.selectById(id);
        diagnosisPO.setNotes(diagnosisPutDTO.getNotes());
        diagnosisMapper.updateById(diagnosisPO);
        return Result.success(diagnosisPO);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        diagnosisMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/stats")
    public Result<DiagnosisStatusVO> getStatus() {
        log.info("getStatus");
        DiagnosisStatusVO res = diagnosisService.getDiagnosisStatus();
        return Result.success(res);
    }

}
