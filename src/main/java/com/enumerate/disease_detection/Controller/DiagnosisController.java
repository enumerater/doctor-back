package com.enumerate.disease_detection.Controller;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.Local.UserContextHolder;
import com.enumerate.disease_detection.Mapper.DiagnosisMapper;
import com.enumerate.disease_detection.POJO.DTO.DiagnosisDTO;
import com.enumerate.disease_detection.POJO.DTO.DiagnosisPutDTO;
import com.enumerate.disease_detection.POJO.PO.DiagnosisPO;
import com.enumerate.disease_detection.POJO.VO.DiagnosisStatusVO;
import com.enumerate.disease_detection.POJO.VO.DiagnosisVO;
import com.enumerate.disease_detection.Service.DiagnosisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public Result<List<DiagnosisVO>> list() {
        log.info("list");
        List<DiagnosisVO> res = diagnosisService.list();
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
                .confidence(res.getConfidence())
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
    public Result<DiagnosisPO> save(@RequestBody DiagnosisDTO diagnosisDTO) {
        log.info("save");
        DiagnosisPO diagnosisPO = DiagnosisPO.builder()
                .userId(UserContextHolder.getUserId())
                .imageUrl(diagnosisDTO.getImageUrl())
                .cropType(diagnosisDTO.getCropType())
                .hasDisease(diagnosisDTO.getHasDisease())
                .diseaseName(diagnosisDTO.getDiseaseName())
                .confidence(diagnosisDTO.getConfidence())
                .severity(diagnosisDTO.getSeverity())
                .result(diagnosisDTO.getResult())
                .status(diagnosisDTO.getStatus())
                .elapsedTime(diagnosisDTO.getElapsedTime())
                .build();
        diagnosisMapper.insert(diagnosisPO);
        return Result.success(diagnosisPO);
    }

    @PutMapping("/{id}")
    public Result<DiagnosisPO> update(@PathVariable String id,@RequestBody DiagnosisPutDTO diagnosisPutDTO) {
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
