package com.enumerate.disease_detection.MVC.POJO.PO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("memory_process_log")
public class MemoryProcessLogPO {
    private Long id;
    private Long userId;
    private String sessionId;
    private LocalDateTime processedAt;
    private Integer memoryCount;
    private String status;
    private String errorMessage;
}
