package com.enumerate.disease_detection.POJO.PO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("vector_store")
public class VectorStorePO {
    private String id;
    private String documentPath;
    private String textContent;
    private String embedding;

}
