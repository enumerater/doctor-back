package com.enumerate.disease_detection.Service;


import com.enumerate.disease_detection.Mapper.DiagnosisMapper;
import com.enumerate.disease_detection.POJO.PO.DiagnosisPO;
import com.enumerate.disease_detection.POJO.VO.CropDistribution;
import com.enumerate.disease_detection.POJO.VO.DiagnosisStatusVO;
import com.enumerate.disease_detection.POJO.VO.DiagnosisVO;
import com.enumerate.disease_detection.POJO.VO.DiseaseDistributionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DiagnosisService {

    @Autowired
    private DiagnosisMapper diagnosisMapper;


    public List<DiagnosisVO> list() {
        List<DiagnosisPO> li = diagnosisMapper.selectList( null);
        List<DiagnosisVO> res = new ArrayList<>();

        for (DiagnosisPO diagnosis : li) {
            DiagnosisVO diagnosisVO = DiagnosisVO.builder()
                    .id(String.valueOf(diagnosis.getId()))
                    .imageUrl(diagnosis.getImageUrl())
                    .cropType(diagnosis.getCropType())
                    .hasDisease(diagnosis.getHasDisease())
                    .diseaseName(diagnosis.getDiseaseName())
                    .confidence(diagnosis.getConfidence())
                    .severity(diagnosis.getSeverity())
                    .result(diagnosis.getResult())
                    .status(diagnosis.getStatus())
                    .createdAt(diagnosis.getCreatedAt())
                    .elapsedTime(String.valueOf(diagnosis.getElapsedTime()))
                    .plotId(diagnosis.getPlotId())
                    .farmId(String.valueOf(diagnosis.getFarmId()))
                    .notes(diagnosis.getNotes())
                    .feedback(diagnosis.getFeedback())
                    .build();
            res.add(diagnosisVO);
        }
        return res;
    }


    public DiagnosisStatusVO getDiagnosisStatus() {
        // 1. 统计总数、患病数、健康数
        Map<String, Object> statusMap = diagnosisMapper.countTotalStatus();
        Long total = ((Number) statusMap.get("total")).longValue();
        Long diseased = ((Number) statusMap.get("diseased")).longValue();
        Long healthy = ((Number) statusMap.get("healthy")).longValue();

        // 2. 查询作物分布
        List<CropDistribution> cropDistribution = diagnosisMapper.selectCropDistribution();

        // 3. 查询病害分布
        List<DiseaseDistributionVO> diseaseDistribution = diagnosisMapper.selectDiseaseDistribution();

        // 4. 组装VO（使用Builder模式）
        return DiagnosisStatusVO.builder()
                .total(String.valueOf(total))
                .diseased(String.valueOf(diseased))
                .healthy(String.valueOf(healthy))
                .cropDistribution(cropDistribution)
                .diseaseDistributionVO(diseaseDistribution) // 注意字段名和JSON的对应
                .build();
    }
}
