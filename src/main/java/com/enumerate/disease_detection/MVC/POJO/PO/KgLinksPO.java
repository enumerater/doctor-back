package com.enumerate.disease_detection.MVC.POJO.PO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("kg_links")
public class KgLinksPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sourceId;
    private String targetId;
    private String relation;
}
