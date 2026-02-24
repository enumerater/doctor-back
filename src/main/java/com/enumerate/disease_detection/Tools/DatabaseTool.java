package com.enumerate.disease_detection.Tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enumerate.disease_detection.Mapper.DiagnosisMapper;
import com.enumerate.disease_detection.Mapper.DiseasesMapper;
import com.enumerate.disease_detection.Mapper.FarmMapper;
import com.enumerate.disease_detection.Mapper.PlotMapper;
import com.enumerate.disease_detection.Mapper.SeasonalMapper;
import com.enumerate.disease_detection.POJO.PO.*;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DatabaseTool {

    @Autowired
    private DiagnosisMapper diagnosisMapper;

    @Autowired
    private FarmMapper farmMapper;

    @Autowired
    private PlotMapper plotMapper;

    @Autowired
    private DiseasesMapper diseasesMapper;

    @Autowired
    private SeasonalMapper seasonalMapper;

    @Tool("查询用户的历史诊断记录，可以了解用户过去的作物病害诊断情况，包括作物类型、病害名称、严重程度、诊断时间等。当用户询问'我之前的诊断记录'、'历史检测结果'、'上次诊断'等问题时应调用此工具。")
    public String queryDiagnosisHistory(
            @P("用户ID") Long userId,
            @P("返回的最大记录数，默认10") int limit) {
        log.info("工具调用: 查询诊断历史, userId={}, limit={}", userId, limit);

        LambdaQueryWrapper<DiagnosisPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiagnosisPO::getUserId, userId)
                .orderByDesc(DiagnosisPO::getCreatedAt)
                .last("LIMIT " + Math.min(limit, 50));

        List<DiagnosisPO> records = diagnosisMapper.selectList(wrapper);

        if (records.isEmpty()) {
            return "该用户暂无诊断记录。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("共找到 %d 条诊断记录：\n", records.size()));
        for (int i = 0; i < records.size(); i++) {
            DiagnosisPO r = records.get(i);
            sb.append(String.format("%d. [%s] 作物: %s | 是否患病: %s | 病害: %s | 严重程度: %s | 诊断结果: %s\n",
                    i + 1,
                    r.getCreatedAt() != null ? r.getCreatedAt() : "未知时间",
                    r.getCropType() != null ? r.getCropType() : "未知",
                    r.getHasDisease() != null && r.getHasDisease() == 1 ? "是" : "否",
                    r.getDiseaseName() != null ? r.getDiseaseName() : "无",
                    r.getSeverity() != null ? r.getSeverity() : "未知",
                    r.getResult() != null ? r.getResult() : "无"));
        }
        return sb.toString();
    }

    @Tool("查询用户的农场和地块信息，包括农场名称、位置、面积，以及各地块的作物类型、播种日期、生长阶段等。当用户询问'我的农场'、'我的地块'、'种了什么'等问题时应调用此工具。")
    public String queryUserFarmInfo(@P("用户ID") String userId) {
        log.info("工具调用: 查询农场信息, userId={}", userId);

        LambdaQueryWrapper<FarmPO> farmWrapper = new LambdaQueryWrapper<>();
        farmWrapper.eq(FarmPO::getUserId, userId);
        List<FarmPO> farms = farmMapper.selectList(farmWrapper);

        if (farms.isEmpty()) {
            return "该用户暂无农场信息。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("共找到 %d 个农场：\n", farms.size()));

        for (FarmPO farm : farms) {
            sb.append(String.format("\n【农场】%s | 位置: %s | 面积: %s | 地块数: %d\n",
                    farm.getName(),
                    farm.getLocation() != null ? farm.getLocation() : "未知",
                    farm.getArea() != null ? farm.getArea() : "未知",
                    farm.getPlotCount() != null ? farm.getPlotCount() : 0));

            LambdaQueryWrapper<PlotPO> plotWrapper = new LambdaQueryWrapper<>();
            plotWrapper.eq(PlotPO::getFarmId, farm.getId());
            List<PlotPO> plots = plotMapper.selectList(plotWrapper);

            if (!plots.isEmpty()) {
                for (PlotPO plot : plots) {
                    sb.append(String.format("  - 地块: %s | 作物: %s | 面积: %s | 播种日期: %s | 土壤: %s | 生长阶段: %s\n",
                            plot.getName() != null ? plot.getName() : "未命名",
                            plot.getCropType() != null ? plot.getCropType() : "未知",
                            plot.getArea() != null ? plot.getArea() : "未知",
                            plot.getSowingDate() != null ? plot.getSowingDate() : "未知",
                            plot.getSoilType() != null ? plot.getSoilType() : "未知",
                            plot.getGrowthStage() != null ? plot.getGrowthStage() : "未知"));
                }
            } else {
                sb.append("  暂无地块信息\n");
            }
        }
        return sb.toString();
    }

    @Tool("搜索病害知识库，根据关键词查找病害的详细信息，包括症状、发病条件、传播途径、防治方法（农业防治、化学防治、生物防治）。当用户询问某种病害的信息、防治方法、症状特征时应调用此工具。")
    public String searchDiseaseKnowledge(@P("搜索关键词，如病害名称、作物名称等") String keyword) {
        log.info("工具调用: 搜索病害知识库, keyword={}", keyword);

        LambdaQueryWrapper<DiseasesPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(DiseasesPO::getName, keyword)
                .or()
                .like(DiseasesPO::getCropName, keyword)
                .or()
                .like(DiseasesPO::getSymptomsText, keyword);

        List<DiseasesPO> diseases = diseasesMapper.selectList(wrapper);

        if (diseases.isEmpty()) {
            return "未找到与\"" + keyword + "\"相关的病害知识。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("找到 %d 条相关病害知识：\n", diseases.size()));

        for (DiseasesPO d : diseases) {
            sb.append(String.format("\n【%s】(作物: %s, 分类: %s, 严重程度: %s)\n",
                    d.getName(),
                    d.getCropName() != null ? d.getCropName() : "未知",
                    d.getCategory() != null ? d.getCategory() : "未知",
                    d.getSeverity() != null ? d.getSeverity() : "未知"));
            if (d.getSymptomsText() != null) {
                sb.append("  症状: ").append(d.getSymptomsText()).append("\n");
            }
            if (d.getConditionsTemperature() != null || d.getConditionsHumidity() != null) {
                sb.append("  发病条件: ");
                if (d.getConditionsTemperature() != null) sb.append("温度 ").append(d.getConditionsTemperature()).append(" ");
                if (d.getConditionsHumidity() != null) sb.append("湿度 ").append(d.getConditionsHumidity()).append(" ");
                if (d.getConditionsSeason() != null) sb.append("季节 ").append(d.getConditionsSeason()).append(" ");
                if (d.getConditionsStage() != null) sb.append("阶段 ").append(d.getConditionsStage());
                sb.append("\n");
            }
            if (d.getTransmission() != null) {
                sb.append("  传播途径: ").append(d.getTransmission()).append("\n");
            }
            if (d.getPreventionAgricultural() != null) {
                sb.append("  农业防治: ").append(d.getPreventionAgricultural()).append("\n");
            }
            if (d.getPreventionChemical() != null) {
                sb.append("  化学防治: ").append(d.getPreventionChemical()).append("\n");
            }
            if (d.getPreventionBiological() != null) {
                sb.append("  生物防治: ").append(d.getPreventionBiological()).append("\n");
            }
        }
        return sb.toString();
    }

    @Tool("查询指定月份的季节性病害风险信息，了解当月高发病害、风险等级和防治建议。当用户询问'这个月容易得什么病'、'当前季节风险'、'应季病害'等问题时应调用此工具。")
    public String querySeasonalRisks(@P("月份，1-12的整数") int month) {
        log.info("工具调用: 查询季节性风险, month={}", month);

        LambdaQueryWrapper<DiseaseSeasonPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiseaseSeasonPO::getMonth, String.valueOf(month));

        List<DiseaseSeasonPO> risks = seasonalMapper.selectList(wrapper);

        if (risks.isEmpty()) {
            return String.format("%d月暂无季节性病害风险记录。", month);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%d月 季节性病害风险（共%d条）：\n", month, risks.size()));

        for (DiseaseSeasonPO r : risks) {
            sb.append(String.format("  - %s（作物: %s）| 风险等级: %s | %s\n",
                    r.getDiseaseName() != null ? r.getDiseaseName() : "未知病害",
                    r.getCropName() != null ? r.getCropName() : "未知",
                    r.getRiskLevel() != null ? r.getRiskLevel() : "未知",
                    r.getDescription() != null ? r.getDescription() : ""));
        }
        return sb.toString();
    }
}
