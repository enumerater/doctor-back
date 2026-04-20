package com.enumerate.disease_detection.MVC.POJO.PO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_long_term_memory")
public class UserLongTermMemoryPO {
    private Long id;
    private Long userId;
    private String content;
    private Long dreamCursor;
    private LocalDateTime updatedAt;
}