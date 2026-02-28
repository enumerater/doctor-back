package com.enumerate.disease_detection.Tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.Annotations.ToolName;
import com.enumerate.disease_detection.MVC.POJO.PO.DiagnosisPO;
import com.enumerate.disease_detection.MVC.POJO.PO.DiseasesPO;
import com.enumerate.disease_detection.MVC.POJO.PO.FarmPO;
import com.enumerate.disease_detection.MVC.POJO.PO.PlotPO;
import com.enumerate.disease_detection.MVC.Mapper.DiagnosisMapper;
import com.enumerate.disease_detection.MVC.Mapper.DiseasesMapper;
import com.enumerate.disease_detection.MVC.Mapper.FarmMapper;
import com.enumerate.disease_detection.MVC.Mapper.PlotMapper;
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


    @Tool("查询用户的历史诊断记录，可以了解用户过去的作物病害诊断情况，包括作物类型、病害名称、严重程度、诊断时间等。当用户询问'我之前的诊断记录'、'历史检测结果'、'上次诊断'等问题时应调用此工具。")
    @ToolName("查询诊断历史")
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
    @ToolName("查询农场信息")
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
    @ToolName("搜索病害知识")
    public String searchDiseaseKnowledge(@P("搜索关键词，如病害名称、作物名称等") String keyword) {
        log.info("工具调用: 搜索病害知识库, keyword={}", keyword);

        QueryWrapper<DiseasesPO> wrapper = new QueryWrapper<>();
        wrapper.like("disease_name", keyword)
                .or()
                .like("crop_name", keyword)
                .or()
                .like("symbol", keyword)
                .last("LIMIT 10");

        List<DiseasesPO> diseases = diseasesMapper.selectList(wrapper);
        log.info("搜索+++++++++++++++++++++{}", keyword);

        if (diseases.isEmpty()) {
            return "未找到与\"" + keyword + "\"相关的病害知识。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("找到 %d 条相关病害知识：\n", diseases.size()));

        for (DiseasesPO d : diseases) {
            sb.append(String.format("\n【%s】(作物: %s, 分类: %s)\n",
                    d.getDiseaseName(),
                    d.getCropName() != null ? d.getCropName() : "未知",
                    d.getCategory() != null ? d.getCategory() : "未知" ));
            if (d.getSymbol() != null) {
                sb.append("  症状: ").append(d.getSymbol()).append("\n");
            }
            if (d.getPrevention() != null) {
                sb.append("  防治: ").append(d.getPrevention()).append("\n");
            }
        }
        return sb.toString();
    }

}
