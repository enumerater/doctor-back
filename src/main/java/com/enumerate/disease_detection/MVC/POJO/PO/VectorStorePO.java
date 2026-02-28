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
@TableName("vector_store")
public class VectorStorePO {
    private String id;
    private Long userId;
    private String documentPath;
    private String textContent;
    private String embedding;
    private String memoryType;
    private String sourceSessionId;
    private LocalDateTime createdAt;
}
