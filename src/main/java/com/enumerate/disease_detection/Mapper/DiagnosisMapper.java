package com.enumerate.disease_detection.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enumerate.disease_detection.POJO.PO.DiagnosisPO;
import com.enumerate.disease_detection.POJO.VO.CropDistribution;
import com.enumerate.disease_detection.POJO.VO.DiseaseDistributionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DiagnosisMapper extends BaseMapper<DiagnosisPO> {
    /**
     * 统计总数、患病数、健康数
     */
    @Select("SELECT " +
            "COUNT(*) AS total, " +
            "SUM(CASE WHEN has_disease = 1 THEN 1 ELSE 0 END) AS diseased, " +
            "SUM(CASE WHEN has_disease = 0 THEN 1 ELSE 0 END) AS healthy " +
            "FROM diagnosis")
    Map<String, Object> countTotalStatus();

    /**
     * 查询作物分布（按crop_type分组统计）
     */
    @Select("SELECT crop_type AS name, COUNT(*) AS count " +
            "FROM diagnosis " +
            "WHERE crop_type != '' " +
            "GROUP BY crop_type")
    List<CropDistribution> selectCropDistribution();

    /**
     * 查询病害分布（按disease_name分组统计，仅统计有病害的记录）
     */
    @Select("SELECT disease_name AS name, COUNT(*) AS count " +
            "FROM diagnosis " +
            "WHERE has_disease = 1 AND disease_name != '' " +
            "GROUP BY disease_name")
    List<DiseaseDistributionVO> selectDiseaseDistribution();

}
