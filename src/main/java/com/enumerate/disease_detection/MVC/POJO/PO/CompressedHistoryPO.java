package com.enumerate.disease_detection.MVC.POJO.PO;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("compressed_history")
public class CompressedHistoryPO {
    private Long id;
    private String sessionId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}
