package com.enumerate.disease_detection.Mapper;

import com.enumerate.disease_detection.POJO.VO.AdminUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminMapper {

    @Select("SELECT COUNT(*) FROM sys_user WHERE deleted = '0'")
    Long countTotalUsers();

    @Select("SELECT COUNT(*) FROM sys_user WHERE deleted = '0' AND DATE(last_login_time) = CURDATE()")
    Long countActiveToday();

    @Select("SELECT COUNT(*) FROM diagnosis")
    Long countTotalDiagnoses();

    @Select("SELECT COUNT(*) FROM diagnosis WHERE DATE(created_at) = CURDATE()")
    Long countDiagnosesToday();

    @Select("SELECT COUNT(*) FROM knowledge")
    Long countKnowledgeEntries();

    @Select("SELECT COUNT(*) FROM feedback")
    Long countFeedback();

    @Select("SELECT COUNT(*) FROM feedback WHERE status = 'pending'")
    Long countFeedbackPending();

    @Select("<script>" +
            "SELECT u.id, u.username, u.role, u.status, u.create_time AS createdAt, " +
            "u.last_login_time AS lastLogin, " +
            "(SELECT COUNT(*) FROM diagnosis d WHERE d.user_id = u.id) AS diagnosisCount " +
            "FROM sys_user u " +
            "WHERE u.deleted = '0' " +
            "<if test='keyword != null'> AND u.username LIKE CONCAT('%', #{keyword}, '%')</if>" +
            "<if test='status != null'>" +
            "  <if test=\"status == 'active'\"> AND u.status = '1'</if>" +
            "  <if test=\"status == 'disabled'\"> AND u.status = '0'</if>" +
            "</if>" +
            "<if test='role != null'>" +
            "  <if test=\"role == 'user'\"> AND u.role = '0'</if>" +
            "  <if test=\"role == 'admin'\"> AND u.role = '1'</if>" +
            "</if>" +
            " ORDER BY u.create_time DESC" +
            "</script>")
    List<AdminUserVO> selectUserList(@Param("keyword") String keyword, @Param("status") String status, @Param("role") String role);

    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_user u " +
            "WHERE u.deleted = '0' " +
            "<if test='keyword != null'> AND u.username LIKE CONCAT('%', #{keyword}, '%')</if>" +
            "<if test='status != null'>" +
            "  <if test=\"status == 'active'\"> AND u.status = '1'</if>" +
            "  <if test=\"status == 'disabled'\"> AND u.status = '0'</if>" +
            "</if>" +
            "<if test='role != null'>" +
            "  <if test=\"role == 'user'\"> AND u.role = '0'</if>" +
            "  <if test=\"role == 'admin'\"> AND u.role = '1'</if>" +
            "</if>" +
            "</script>")
    Long countUserList(@Param("keyword") String keyword, @Param("status") String status, @Param("role") String role);
}
