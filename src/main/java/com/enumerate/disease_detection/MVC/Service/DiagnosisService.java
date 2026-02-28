package com.enumerate.disease_detection.MVC.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enumerate.disease_detection.Local.UserContextHolder;
import com.enumerate.disease_detection.MVC.Mapper.DiagnosisMapper;

import com.enumerate.disease_detection.MVC.POJO.PO.DiagnosisPO;
import com.enumerate.disease_detection.MVC.POJO.VO.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DiagnosisService {

    @Autowired
    private DiagnosisMapper diagnosisMapper;


    // 方法返回值改为通用响应体
    public PageData<DiagnosisVO> list(Integer page, Integer pageSize, String cropType, String resultType) {
        // 1. 获取当前用户ID（核心过滤条件）
        Long userId = UserContextHolder.getUserId();
        // 2. 构建分页对象：处理空值，默认第1页、每页10条
        Page<DiagnosisPO> pageParam = new Page<>(page == null ? 1 : page, pageSize == null ? 10 : pageSize);

        // 3. 构建查询条件
        QueryWrapper<DiagnosisPO> queryWrapper = new QueryWrapper<DiagnosisPO>()
                .eq("user_id", userId); // 必选条件：当前用户

        // 可选条件：cropType非空时筛选
        if (cropType != null && !cropType.trim().isEmpty()) {
            queryWrapper.eq("crop_type", cropType);
        }
        // 可选条件：resultType非空时筛选（字段名需和数据库一致，若为result则改eq("result", resultType)）
        if (resultType != null && !resultType.trim().isEmpty()) {
            Map<String, Integer> nameMap = new HashMap<>();
            nameMap.put("健康作物", 0);
            nameMap.put("不健康作物", 1);

            queryWrapper.eq("has_disease", nameMap.get(resultType));
        }

        // 4. 执行MyBatis Plus分页查询（核心：selectPage会返回总条数）
        IPage<DiagnosisPO> diagnosisPage = diagnosisMapper.selectPage(pageParam, queryWrapper);

        // 5. PO转换为VO
        List<DiagnosisVO> voList = diagnosisPage.getRecords().stream()
                .map(diagnosis -> DiagnosisVO.builder()
                        .id(String.valueOf(diagnosis.getId()))
                        .imageUrl(diagnosis.getImageUrl())
                        .cropType(diagnosis.getCropType())
                        .hasDisease(diagnosis.getHasDisease())
                        .diseaseName(diagnosis.getDiseaseName())
                        .severity(diagnosis.getSeverity())
                        .result(diagnosis.getResult())
                        .status(diagnosis.getStatus())
                        .createdAt(diagnosis.getCreatedAt())
                        .elapsedTime(String.valueOf(diagnosis.getElapsedTime()))
                        .plotId(diagnosis.getPlotId())
                        .farmId(String.valueOf(diagnosis.getFarmId()))
                        .notes(diagnosis.getNotes())
                        .feedback(diagnosis.getFeedback())
                        .build())
                .collect(Collectors.toList());

        // 6. 封装分页数据（list+total）
        PageData<DiagnosisVO> pageData = new PageData<>();
        pageData.setList(voList);
        pageData.setTotal(diagnosisPage.getTotal()); // 获取MyBatis Plus查询返回的总条数

        return pageData;
    }


    public DiagnosisStatusVO getDiagnosisStatus() {
        Long userId = UserContextHolder.getUserId();
        // 1. 统计总数、患病数、健康数
        Map<String, Object> statusMap = diagnosisMapper.countTotalStatus(userId);
        Long total = statusMap.get("total") != null ? ((Number) statusMap.get("total")).longValue() : 0L;
        Long diseased = statusMap.get("diseased") != null ? ((Number) statusMap.get("diseased")).longValue() : 0L;
        Long healthy = statusMap.get("healthy") != null ? ((Number) statusMap.get("healthy")).longValue() : 0L;
        Long nonCrop = statusMap.get("nonCrop") != null ? ((Number) statusMap.get("nonCrop")).longValue() : 0L;

        // 2. 查询作物分布
        List<CropDistribution> cropDistribution = diagnosisMapper.selectCropDistribution(userId);

        // 3. 查询病害分布
        List<DiseaseDistributionVO> diseaseDistribution = diagnosisMapper.selectDiseaseDistribution(userId);

        // 4. 组装VO（使用Builder模式）
        return DiagnosisStatusVO.builder()
                .total(String.valueOf(total))
                .diseased(String.valueOf(diseased))
                .healthy(String.valueOf(healthy))
                .cropDistribution(cropDistribution)
                .diseaseDistributionVO(diseaseDistribution) // 注意字段名和JSON的对应
                .nonCrop(String.valueOf(nonCrop))
                .build();
    }
}
