package com.enumerate.disease_detection.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enumerate.disease_detection.POJO.PO.FeedbackPO;
import com.enumerate.disease_detection.POJO.VO.FeedbackVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface FeedbackMapper extends BaseMapper<FeedbackPO> {

    @Select("<script>" +
            "SELECT f.id, f.diagnosis_id, f.accuracy, f.correct_disease, f.rating, f.comment, " +
            "u.username, f.crop_type, f.diagnosed_disease, f.status, f.created_at " +
            "FROM feedback f LEFT JOIN sys_user u ON f.user_id = u.id " +
            "<where>" +
            "<if test='status != null'> AND f.status = #{status}</if>" +
            "<if test='accuracy != null'> AND f.accuracy = #{accuracy}</if>" +
            "</where>" +
            " ORDER BY f.created_at DESC" +
            "</script>")
    List<FeedbackVO> selectFeedbackList(@Param("status") String status, @Param("accuracy") String accuracy);

    @Select("SELECT " +
            "COUNT(*) AS total, " +
            "SUM(CASE WHEN accuracy = 'correct' THEN 1 ELSE 0 END) AS correct, " +
            "SUM(CASE WHEN accuracy = 'partial' THEN 1 ELSE 0 END) AS partial, " +
            "SUM(CASE WHEN accuracy = 'incorrect' THEN 1 ELSE 0 END) AS incorrect, " +
            "AVG(rating) AS avgRating " +
            "FROM feedback")
    Map<String, Object> selectFeedbackStats();
}
