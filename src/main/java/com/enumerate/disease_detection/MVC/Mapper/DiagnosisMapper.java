package com.enumerate.disease_detection.MVC.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.DiagnosisPO;
import com.enumerate.disease_detection.MVC.POJO.VO.CropDistribution;
import com.enumerate.disease_detection.MVC.POJO.VO.DiseaseDistributionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DiagnosisMapper extends BaseMapper<DiagnosisPO> {
    /**
     * 统计总数、患病数、健康数（按用户过滤）
     */
    @Select("SELECT " +
            "COUNT(*) AS total, " +                    // 总记录数
            "SUM(CASE WHEN has_disease = 1 THEN 1 ELSE 0 END) AS diseased, " +  // 有病作物数
            "SUM(CASE WHEN has_disease = 0 THEN 1 ELSE 0 END) AS healthy, " +   // 健康作物数
            "SUM(CASE WHEN has_disease = 2 THEN 1 ELSE 0 END) AS nonCrop " +    // 新增：非作物数
            "FROM diagnosis WHERE user_id = #{userId}")
    Map<String, Object> countTotalStatus(@Param("userId") Long userId);

    /**
     * 查询作物分布（按crop_type分组统计，按用户过滤）
     */
    @Select("SELECT crop_type AS name, COUNT(*) AS count " +
            "FROM diagnosis " +
            "WHERE user_id = #{userId} AND crop_type != '' " +
            "GROUP BY crop_type")
    List<CropDistribution> selectCropDistribution(@Param("userId") Long userId);

    /**
     * 查询病害分布（按disease_name分组统计，仅统计有病害的记录，按用户过滤）
     */
    @Select("SELECT disease_name AS name, COUNT(*) AS count " +
            "FROM diagnosis " +
            "WHERE user_id = #{userId} AND has_disease = 1 AND disease_name != '' " +
            "GROUP BY disease_name")
    List<DiseaseDistributionVO> selectDiseaseDistribution(@Param("userId") Long userId);

}
