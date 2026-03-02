package com.enumerate.disease_detection.MVC.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.MVC.POJO.PO.MemoryProcessLogPO;
import com.enumerate.disease_detection.MVC.Service.MemoryProcessLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/log")
@CrossOrigin
public class MemoryProcessLogController {

    @Autowired
    private MemoryProcessLogService memoryProcessLogService;

    /**
     * 分页查询日志
     * GET /api/log/page?page=1&size=10&status=SUCCESS
     */
    @GetMapping("/page")
    public Result<IPage<MemoryProcessLogPO>> getLogPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        IPage<MemoryProcessLogPO> logPage = memoryProcessLogService.getLogPage(page, size, userId, status, startTime, endTime);
        return Result.success(logPage);
    }

    /**
     * 获取状态分布统计（用于饼图）
     * GET /api/log/stats/status
     */
    @GetMapping("/stats/status")
    public Result<Map<String, Long>> getStatusStats() {
        return Result.success(memoryProcessLogService.getStatusStats());
    }

    /**
     * 获取处理趋势统计（用于折线图/柱状图）
     * GET /api/log/stats/trend?days=7
     */
    @GetMapping("/stats/trend")
    public Result<List<Map<String, Object>>> getProcessingTrend(
            @RequestParam(defaultValue = "7") int days) {
        return Result.success(memoryProcessLogService.getProcessingTrend(days));
    }
}
