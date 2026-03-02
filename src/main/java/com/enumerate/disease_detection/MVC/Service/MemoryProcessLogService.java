package com.enumerate.disease_detection.MVC.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enumerate.disease_detection.MVC.Mapper.MemoryProcessLogMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.MemoryProcessLogPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MemoryProcessLogService {

    @Autowired
    private MemoryProcessLogMapper memoryProcessLogMapper;

    /**
     * 分页查询日志
     */
    public IPage<MemoryProcessLogPO> getLogPage(int page, int size, Long userId, String status, LocalDateTime startTime, LocalDateTime endTime) {
        Page<MemoryProcessLogPO> pageParam = new Page<>(page, size);
        QueryWrapper<MemoryProcessLogPO> queryWrapper = new QueryWrapper<>();
        
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        if (startTime != null) {
            queryWrapper.ge("processed_at", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("processed_at", endTime);
        }
        
        queryWrapper.orderByDesc("processed_at");
        return memoryProcessLogMapper.selectPage(pageParam, queryWrapper);
    }

    /**
     * 获取状态统计（SUCCESS vs FAILURE）
     */
    public Map<String, Long> getStatusStats() {
        QueryWrapper<MemoryProcessLogPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("status", "count(*) as count");
        queryWrapper.groupBy("status");
        
        List<Map<String, Object>> list = memoryProcessLogMapper.selectMaps(queryWrapper);
        Map<String, Long> stats = new HashMap<>();
        for (Map<String, Object> item : list) {
            stats.put((String) item.get("status"), (Long) item.get("count"));
        }
        return stats;
    }

    /**
     * 获取处理趋势（按天统计处理数量和记忆提取数量）
     */
    public List<Map<String, Object>> getProcessingTrend(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        
        // 由于不同数据库对日期提取函数支持不同，这里先简单按 processed_at 过滤，
        // 然后在内存中聚合，或者写自定义 SQL。
        // 为了兼容性，我们先查询最近 N 天的数据
        QueryWrapper<MemoryProcessLogPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("processed_at", startTime);
        queryWrapper.orderByAsc("processed_at");
        List<MemoryProcessLogPO> logs = memoryProcessLogMapper.selectList(queryWrapper);

        // 按日期分组聚合
        return logs.stream()
                .collect(Collectors.groupingBy(log -> log.getProcessedAt().toLocalDate()))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", entry.getKey().toString());
                    map.put("logCount", (long) entry.getValue().size());
                    map.put("totalMemoryCount", entry.getValue().stream().mapToLong(MemoryProcessLogPO::getMemoryCount).sum());
                    return map;
                })
                .sorted((a, b) -> ((String) a.get("date")).compareTo((String) b.get("date")))
                .collect(Collectors.toList());
    }
}
