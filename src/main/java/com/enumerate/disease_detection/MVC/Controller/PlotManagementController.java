package com.enumerate.disease_detection.MVC.Controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.MVC.POJO.DTO.AiGenerateNoteDTO;
import com.enumerate.disease_detection.MVC.POJO.PO.PesticideRecordPO;
import com.enumerate.disease_detection.MVC.POJO.PO.FieldNotePO;
import com.enumerate.disease_detection.MVC.Service.PlotManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
@Slf4j
public class PlotManagementController {

    @Autowired
    private PlotManagementService plotManagementService;

    // --- 施药管理 ---

    @GetMapping("/plots/{plotId}/pesticide-records")
    public Result<List<PesticideRecordPO>> getPesticideRecords(@PathVariable Long plotId) {
        return Result.success(plotManagementService.getPesticideRecords(plotId));
    }

    @PostMapping("/plots/{plotId}/pesticide-records")
    public Result<String> addPesticideRecord(@PathVariable Long plotId, @RequestBody PesticideRecordPO record) {
        plotManagementService.addPesticideRecord(plotId, record);
        return Result.success("添加成功");
    }

    @PutMapping("/plots/{plotId}/pesticide-records/{id}/effect")
    public Result<String> updatePesticideEffect(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Integer evaluation = (Integer) body.get("effectEvaluation");
        String remarks = (String) body.get("effectRemarks");
        plotManagementService.updatePesticideEffect(id, evaluation, remarks);
        return Result.success("评价成功");
    }

    @PutMapping("/plots/{plotId}/pesticide-records/{id}")
    public Result<String> updatePesticide(
            @PathVariable Long plotId,
            @PathVariable Long id,
            @RequestBody PesticideRecordPO record) {
        plotManagementService.updatePesticide(plotId, id, record);
        return Result.success("修改成功");
    }

    @DeleteMapping("/plots/{plotId}/pesticide-records/{id}")
    public Result<String> deletePesticide(@PathVariable Long id) {
        plotManagementService.deletePesticide(id);
        return Result.success("删除成功");
    }



    // --- 田间随笔 ---

    @GetMapping("/plots/{plotId}/notes")
    public Result<Map<String, Object>> getFieldNotes(
            @PathVariable Long plotId,
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<FieldNotePO> result = plotManagementService.getFieldNotes(plotId, month, page, pageSize);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return Result.success(data);
    }

    @PostMapping("/plots/{plotId}/notes")
    public Result<String> addFieldNote(@PathVariable Long plotId, @RequestBody FieldNotePO note) {
        plotManagementService.addFieldNote(plotId, note);
        return Result.success("发布成功");
    }

    @DeleteMapping("/plots/{plotId}/notes/{id}")
    public Result<String> deleteFieldNote(@PathVariable Long id) {
        plotManagementService.deleteFieldNote(id);
        return Result.success("删除成功");
    }

    @PutMapping("/plots/{plotId}/notes/{id}")
    public Result<String> updateFieldNote(
            @PathVariable Long plotId,
            @PathVariable Long id,
            @RequestBody FieldNotePO record) {
        plotManagementService.updateFieldNote(plotId, id, record);
        return Result.success("修改成功");
    }

    // --- AI 辅助 ---

    @PostMapping("/ai/generate-field-note")
    public Result<Map<String, String>> generateFieldNote(@RequestBody AiGenerateNoteDTO dto) {
        String content = plotManagementService.generateFieldNote(dto.getPlotId(), dto.getTheme(), dto.getKeywords(), dto.getContext());
        Map<String, String> res = new HashMap<>();
        res.put("suggestedContent", content);
        return Result.success(res);
    }
}
