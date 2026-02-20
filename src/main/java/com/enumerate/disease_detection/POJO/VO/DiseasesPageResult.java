package com.enumerate.disease_detection.POJO.VO;

import com.enumerate.disease_detection.POJO.PO.DiseasesPO;
import lombok.Data;

import java.util.List;

@Data
public class DiseasesPageResult {
    // 分页后的病害列表
    private List<DiseasesPO> list;
    // 总条数（改为Long类型，适配MyBatis-Plus分页返回的总数类型）
    private int total;
}
