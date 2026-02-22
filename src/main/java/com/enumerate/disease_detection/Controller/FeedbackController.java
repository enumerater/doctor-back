package com.enumerate.disease_detection.Controller;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.POJO.DTO.FeedbackDTO;
import com.enumerate.disease_detection.POJO.DTO.FeedbackStatusDTO;
import com.enumerate.disease_detection.POJO.PO.FeedbackPO;
import com.enumerate.disease_detection.POJO.VO.FeedbackStatsVO;
import com.enumerate.disease_detection.POJO.VO.FeedbackVO;
import com.enumerate.disease_detection.Service.FeedbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feedback")
@Slf4j
@CrossOrigin
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping
    public Result<FeedbackPO> submit(@RequestBody FeedbackDTO dto) {
        log.info("submit feedback: {}", dto);
        FeedbackPO result = feedbackService.submit(dto);
        return Result.success(result);
    }

    @GetMapping("/diagnosis/{diagnosisId}")
    public Result<FeedbackPO> getByDiagnosisId(@PathVariable String diagnosisId) {
        log.info("getByDiagnosisId: {}", diagnosisId);
        FeedbackPO result = feedbackService.getByDiagnosisId(diagnosisId);
        return Result.success(result);
    }

    @GetMapping("/list")
    public Result<List<FeedbackVO>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String accuracy) {
        log.info("feedback list: status={}, accuracy={}", status, accuracy);
        List<FeedbackVO> result = feedbackService.list(status, accuracy);
        return Result.success(result);
    }

    @PutMapping("/{id}/status")
    public Result<FeedbackPO> updateStatus(@PathVariable Long id, @RequestBody FeedbackStatusDTO dto) {
        log.info("updateStatus: id={}, status={}", id, dto.getStatus());
        FeedbackPO result = feedbackService.updateStatus(id, dto.getStatus());
        return Result.success(result);
    }

    @GetMapping("/stats")
    public Result<FeedbackStatsVO> stats() {
        log.info("feedback stats");
        FeedbackStatsVO result = feedbackService.stats();
        return Result.success(result);
    }
}
