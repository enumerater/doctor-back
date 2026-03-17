package com.enumerate.disease_detection.Tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.Annotations.ToolName;
import com.enumerate.disease_detection.MVC.POJO.PO.DiagnosisPO;
import com.enumerate.disease_detection.MVC.POJO.PO.DiseasesPO;
import com.enumerate.disease_detection.MVC.POJO.PO.FarmPO;
import com.enumerate.disease_detection.MVC.POJO.PO.PlotPO;
import com.enumerate.disease_detection.MVC.Mapper.*;
import com.enumerate.disease_detection.MVC.POJO.PO.*;
import com.enumerate.disease_detection.MVC.POJO.VO.KgGraphVO;
import com.enumerate.disease_detection.MVC.Service.KnowledgeService;
import com.enumerate.disease_detection.MVC.Service.PlotManagementService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private KnowledgeService knowledgeService;

    @Autowired
    private PlotManagementService plotManagementService;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private FieldNoteMapper fieldNoteMapper;

    @Autowired
    private PesticideRecordMapper pesticideRecordMapper;

    // ... (keep searchKnowledgeGraph, queryDiagnosisHistory, queryUserFarmInfo, createFarm, updateFarm, deleteFarm, createPlot, updatePlot, deletePlot)

    @Tool("查询地块的施药记录。当用户想了解某个地块过去用了什么药、用药量、效果如何时调用。")
    @ToolName("query_pesticide_records")
    public String queryPesticideRecords(@P("地块ID") String plotId) {
        log.info("工具调用: 查询施药记录, plotId={}", plotId);
        List<PesticideRecordPO> records = plotManagementService.getPesticideRecords(Long.valueOf(plotId));
        if (records.isEmpty()) return "该地块暂无施药记录。";

        StringBuilder sb = new StringBuilder("施药记录：\n");
        for (PesticideRecordPO r : records) {
            sb.append(String.format("- ID: %d | 日期: %s | 药名: %s | 分类: %s | 用量: %s %s | 目的: %s | 评价: %s/5\n",
                    r.getId(), r.getApplicationDate(), r.getMedicineName(), r.getCategory(),
                    r.getDosage(), r.getUnit(), r.getPurpose(),
                    r.getEffectEvaluation() != null ? r.getEffectEvaluation() : "未评价"));
        }
        return sb.toString();
    }

    @Tool("添加施药记录。需要地块ID、药剂名称、分类（杀虫剂/杀菌剂/除草剂/肥料）、用量、单位、施用日期（YYYY-MM-DD）等。调用前请先确认。")
    @ToolName("add_pesticide_record")
    public String addPesticideRecord(
            @P("地块ID") String plotId,
            @P("药剂名称") String medicineName,
            @P("分类") String category,
            @P("用量") String dosage,
            @P("单位") String unit,
            @P("施用日期 (YYYY-MM-DD)") String date,
            @P("施用目的") String purpose) {
        log.info("工具调用: 添加施药记录, plotId={}, medicineName={}", plotId, medicineName);
        PesticideRecordPO record = PesticideRecordPO.builder()
                .plotId(Long.valueOf(plotId))
                .medicineName(medicineName)
                .category(category)
                .dosage(Float.valueOf(dosage))
                .unit(unit)
                .applicationDate(LocalDate.parse(date))
                .purpose(purpose)
                .build();
        plotManagementService.addPesticideRecord(Long.valueOf(plotId), record);
        return "施药记录已成功添加。";
    }

    @Tool("更新施药记录。可以修改药名、用量、评价等。调用前请先确认。")
    @ToolName("update_pesticide_record")
    public String updatePesticideRecord(
            @P("记录ID") String recordId,
            @P("地块ID") String plotId,
            @P("药剂名称") String medicineName,
            @P("用量") String dosage,
            @P("效果评价(1-5)") String evaluation,
            @P("评价备注") String remarks) {
        log.info("工具调用: 更新施药记录, recordId={}", recordId);
        PesticideRecordPO record = pesticideRecordMapper.selectById(recordId);
        if (record == null) return "未找到该记录。";
        if (medicineName != null) record.setMedicineName(medicineName);
        if (dosage != null) record.setDosage(Float.valueOf(dosage));
        if (evaluation != null) record.setEffectEvaluation(Integer.valueOf(evaluation));
        if (remarks != null) record.setEffectRemarks(remarks);
        plotManagementService.updatePesticide(plotId, recordId, record);
        return "施药记录已更新。";
    }

    @Tool("删除施药记录。需要记录ID。调用前请先确认。")
    @ToolName("delete_pesticide_record")
    public String deletePesticideRecord(@P("记录ID") Long recordId) {
        log.info("工具调用: 删除施药记录, recordId={}", recordId);
        plotManagementService.deletePesticide(recordId);
        return "施药记录已删除。";
    }

    @Tool("查询地块的田间随笔。可以按月份筛选（YYYY-MM）。")
    @ToolName("query_field_notes")
    public String queryFieldNotes(
            @P("地块ID") String plotId,
            @P("查询月份 (YYYY-MM)，为空则查询所有") String month) {
        log.info("工具调用: 查询田间随笔, plotId={}, month={}", plotId, month);
        var page = plotManagementService.getFieldNotes(Long.valueOf(plotId), month, 1, 50);
        List<FieldNotePO> notes = page.getRecords();
        if (notes.isEmpty()) return "该地块暂无随笔。";

        StringBuilder sb = new StringBuilder("田间随笔：\n");
        for (FieldNotePO n : notes) {
            sb.append(String.format("- ID: %d | 日期: %s | 天气: %s | 内容: %s\n",
                    n.getId(), n.getDate(), n.getWeatherInfo(), n.getContent()));
        }
        return sb.toString();
    }

    @Tool("添加田间随笔。需要地块ID、随笔内容、日期（YYYY-MM-DD）。调用前请先确认。")
    @ToolName("add_field_note")
    public String addFieldNote(
            @P("地块ID") String plotId,
            @P("随笔内容") String content,
            @P("日期 (YYYY-MM-DD)") String date,
            @P("天气信息") String weather) {
        log.info("工具调用: 添加随笔, plotId={}", plotId);
        FieldNotePO note = FieldNotePO.builder()
                .plotId(Long.valueOf(plotId))
                .content(content)
                .date(LocalDate.parse(date))
                .weatherInfo(weather)
                .isAiGenerated(false)
                .build();
        plotManagementService.addFieldNote(Long.valueOf(plotId), note);
        return "随笔已成功添加。";
    }

    @Tool("更新田间随笔。可以修改内容。调用前请先确认。")
    @ToolName("update_field_note")
    public String updateFieldNote(
            @P("随笔ID") String noteId,
            @P("地块ID") String plotId,
            @P("新内容") String content) {
        log.info("工具调用: 更新随笔, noteId={}", noteId);
        FieldNotePO note = fieldNoteMapper.selectById(noteId);
        if (note == null) return "未找到该随笔。";
        note.setContent(content);
        plotManagementService.updateFieldNote(plotId, noteId, note);
        return "随笔内容已更新。";
    }

    @Tool("删除田间随笔。需要随笔ID。调用前请先确认。")
    @ToolName("delete_field_note")
    public String deleteFieldNote(@P("随笔ID") String noteId) {
        log.info("工具调用: 删除随笔, noteId={}", noteId);
        plotManagementService.deleteFieldNote(Long.valueOf(noteId));
        return "随笔已删除。";
    }

    @Tool("查询用户的通知。可以了解病害预警、系统通知等。")
    @ToolName("query_notifications")
    public String queryNotifications(@P("用户ID") String userId) {
        log.info("工具调用: 查询通知, userId={}", userId);
        List<NotificationPO> notifications = notificationMapper.selectList(new QueryWrapper<NotificationPO>()
                .eq("user_id", userId)
                .orderByDesc("created_at")
                .last("LIMIT 20"));

        if (notifications.isEmpty()) return "暂无通知。";

        StringBuilder sb = new StringBuilder("通知列表：\n");
        for (NotificationPO n : notifications) {
            sb.append(String.format("- ID: %d | [%s] %s: %s | 状态: %s | 时间: %s\n",
                    n.getId(), n.getType(), n.getTitle(), n.getContent(),
                    (n.getIsRead() != null && n.getIsRead()) ? "已读" : "未读",
                    n.getCreatedAt()));
        }
        return sb.toString();
    }

    @Tool("删除通知。需要通知ID。调用前请先确认。")
    @ToolName("delete_notification")
    public String deleteNotification(@P("通知ID") String notificationId) {
        log.info("工具调用: 删除通知, notificationId={}", notificationId);
        int result = notificationMapper.deleteById(notificationId);
        return result > 0 ? "通知已删除。" : "删除失败，未找到该通知。";
    }


    @Tool("搜索农业知识图谱，查找特定作物（如柑橘）下的关键词及其关联的知识点（如病害、药剂、节气等）。当需要了解节点之间的关系或进行深度关联分析时调用。")
    @ToolName("knowledge_graph")
    public String searchKnowledgeGraph(
            @P("作物名称，如：柑橘") String cropName,
            @P("搜索关键词，如：病害名、药剂名") String keyword) {
        log.info("工具调用: 搜索知识图谱, cropName={}, keyword={}", cropName, keyword);

        KgGraphVO graph = knowledgeService.getKnowledgeGraph(cropName, keyword, null, 2);

        if (graph.getNodes().isEmpty()) {
            return "在知识图谱中未找到与\"" + keyword + "\"相关的节点。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("在知识图谱中找到 %d 个节点和 %d 条关系：\n",
                graph.getNodes().size(), graph.getLinks().size()));

        sb.append("\n【节点信息】\n");
        for (var node : graph.getNodes()) {
            sb.append(String.format("- [%s] %s: %s\n",
                    node.getType(), node.getName(),
                    node.getDetails() != null ? node.getDetails() : "暂无详情"));
        }

        if (!graph.getLinks().isEmpty()) {
            sb.append("\n【关联关系】\n");
            for (var link : graph.getLinks()) {
                String sourceName = graph.getNodes().stream()
                        .filter(n -> n.getId().equals(link.getSource()))
                        .findFirst().map(n -> n.getName()).orElse(link.getSource());
                String targetName = graph.getNodes().stream()
                        .filter(n -> n.getId().equals(link.getTarget()))
                        .findFirst().map(n -> n.getName()).orElse(link.getTarget());
                sb.append(String.format("- %s --(%s)--> %s\n", sourceName, link.getRelation(), targetName));
            }
        }

        return sb.toString();
    }


    @Tool("查询用户的历史诊断记录，可以了解用户过去的作物病害诊断情况，包括作物类型、病害名称、严重程度、诊断时间等。当用户询问'我之前的诊断记录'、'历史检测结果'、'上次诊断'等问题时应调用此工具。")
    @ToolName("diagnosis_history")
    public String queryDiagnosisHistory(
            @P("用户ID") String userId,
            @P("返回的最大记录数，默认10") String limit) {
        log.info("工具调用: 查询诊断历史, userId={}, limit={}", userId, limit);

        LambdaQueryWrapper<DiagnosisPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiagnosisPO::getUserId, userId)
                .orderByDesc(DiagnosisPO::getCreatedAt)
                .last("LIMIT " + Math.min(Integer.parseInt(limit), 50));

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
    @ToolName("farm_info")
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
            sb.append(String.format("\n【农场】id: %s | %s | 位置: %s | 面积: %s | 地块数: %d\n",
                    farm.getId(),
                    farm.getName(),
                    farm.getLocation() != null ? farm.getLocation() : "未知",
                    farm.getArea() != null ? farm.getArea() : "未知",
                    farm.getPlotCount() != null ? farm.getPlotCount() : 0));

            LambdaQueryWrapper<PlotPO> plotWrapper = new LambdaQueryWrapper<>();
            plotWrapper.eq(PlotPO::getFarmId, farm.getId());
            List<PlotPO> plots = plotMapper.selectList(plotWrapper);

            if (!plots.isEmpty()) {
                for (PlotPO plot : plots) {
                    sb.append(String.format("  - 地块:id: %s | %s | 作物: %s | 面积: %s | 播种日期: %s | 土壤: %s | 生长阶段: %s\n",
                            plot.getId(),
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

    @Tool("创建新农场。当用户明确表示要创建一个新农场，并且提供了农场名称、位置、面积等信息时调用。调用此工具前，应确保已经通过'confirm_action'工具获得了用户的确认。")
    @ToolName("create_farm")
    public String createFarm(
            @P("农场名称") String name,
            @P("农场位置") String location,
            @P("农场面积（只提取数字，单位默认为'亩'。如果用户说'100亩'，请提取数值100；如果用户只说数字，则视为'亩'）") String area,
            @P("用户ID") String userId) {
        log.info("工具调用: 创建农场, name={}, location={}, area={}, userId={}", name, location, area, userId);

        FarmPO farm = FarmPO.builder()
                .name(name)
                .location(location)
                .area(Double.valueOf(area))
                .userId(Long.valueOf(userId))
                .plotCount(0)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();

        int result = farmMapper.insert(farm);
        if (result > 0) {
            return String.format("农场\"%s\"创建成功，ID为: %d", name, farm.getId());
        } else {
            return "农场创建失败。";
        }
    }

    @Tool("更新农场信息。当用户想要修改现有农场的名称、位置或面积时调用。调用此工具前，应确保已经通过'confirm_action'工具获得了用户的确认。")
    @ToolName("update_farm")
    public String updateFarm(
            @P("农场ID") String farmId,
            @P("新农场名称") String name,
            @P("新农场位置") String location,
            @P("新农场面积") String area) {
        log.info("工具调用: 更新农场, farmId={}, name={}, location={}, area={}", farmId, name, location, area);

        FarmPO farm = farmMapper.selectById(farmId);
        if (farm == null) return "未找到ID为" + farmId + "的农场。";

        if (name != null) farm.setName(name);
        if (location != null) farm.setLocation(location);
        if (area != null) farm.setArea(Double.valueOf(area));
        farm.setUpdatedAt(java.time.LocalDateTime.now());

        int result = farmMapper.updateById(farm);
        return result > 0 ? "农场信息更新成功。" : "农场信息更新失败。";
    }

    @Tool("删除农场。这是一个高危操作，会删除农场及其下的所有地块。必须在获得用户明确确认后才能调用。")
    @ToolName("delete_farm")
    public String deleteFarm(@P("农场ID") String farmId) {
        log.info("工具调用: 删除农场, farmId={}", farmId);

        // 查找农场
        FarmPO farm = farmMapper.selectById(farmId);
        if (farm == null) return "未找到ID为" + farmId + "的农场。";

        // 硬删除该农场下的所有地块
        LambdaQueryWrapper<PlotPO> plotWrapper = new LambdaQueryWrapper<>();
        plotWrapper.eq(PlotPO::getFarmId, farmId);
        plotMapper.delete(plotWrapper);

        // 硬删除农场
        int result = farmMapper.deleteById(farmId);

        return result > 0 ? "农场及其关联地块已成功彻底删除。" : "农场删除失败。";
    }

    @Tool("在农场中创建新地块。需要提供农场ID、地块名称、作物类型、面积、播种日期、土壤类型等信息。调用此工具前，应确保已经通过'confirm_action'工具获得了用户的确认。")
    @ToolName("create_plot")
    public String createPlot(
            @P("所属农场ID") String farmId,
            @P("地块名称") String name,
            @P("作物类型（如：小麦、玉米）") String cropType,
            @P("地块面积") String area,
            @P("播种日期（YYYY-MM-DD）") String sowingDate,
            @P("土壤类型（如：黑土、沙壤土）") String soilType) {
        log.info("工具调用: 创建地块, farmId={}, name={}, cropType={}", farmId, name, cropType);

        PlotPO plot = PlotPO.builder()
                .farmId(Long.valueOf(farmId))
                .name(name)
                .cropType(cropType)
                .area(Double.valueOf(area))
                .sowingDate(sowingDate)
                .soilType(soilType)
                .growthStage("播种期")
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();

        int result = plotMapper.insert(plot);
        if (result > 0) {
            // 更新农场的地块数量
            FarmPO farm = farmMapper.selectById(farmId);
            if (farm != null) {
                farm.setPlotCount((farm.getPlotCount() != null ? farm.getPlotCount() : 0) + 1);
                farmMapper.updateById(farm);
            }
            return String.format("地块\"%s\"创建成功，ID为: %d", name, plot.getId());
        } else {
            return "地块创建失败。";
        }
    }

    @Tool("更新地块信息。可以修改地块名称、作物类型、面积、播种日期、土壤类型、生长阶段等。调用此工具前，应确保已经通过'confirm_action'工具获得了用户的确认。")
    @ToolName("update_plot")
    public String updatePlot(
            @P("地块ID") String plotId,
            @P("新地块名称") String name,
            @P("新作物类型") String cropType,
            @P("新地块面积") String area,
            @P("新播种日期") String sowingDate,
            @P("新土壤类型") String soilType,
            @P("新生长阶段") String growthStage) {
        log.info("工具调用: 更新地块, plotId={}, name={}", plotId, name);

        PlotPO plot = plotMapper.selectById(plotId);
        if (plot == null) return "未找到ID为" + plotId + "的地块。";

        if (name != null) plot.setName(name);
        if (cropType != null) plot.setCropType(cropType);
        if (area != null) plot.setArea(Double.valueOf(area));
        if (sowingDate != null) plot.setSowingDate(sowingDate);
        if (soilType != null) plot.setSoilType(soilType);
        if (growthStage != null) plot.setGrowthStage(growthStage);
        plot.setUpdatedAt(java.time.LocalDateTime.now());

        int result = plotMapper.updateById(plot);
        return result > 0 ? "地块信息更新成功。" : "地块信息更新失败。";
    }

    @Tool("删除地块。这是一个高危操作，必须在获得用户明确确认后才能调用。")
    @ToolName("delete_plot")
    public String deletePlot(@P("地块ID") String plotId) {
        log.info("工具调用: 删除地块, plotId={}", plotId);

        PlotPO plot = plotMapper.selectById(plotId);
        if (plot == null) return "未找到ID为" + plotId + "的地块。";

        // 硬删除地块
        int result = plotMapper.deleteById(plotId);

        if (result > 0) {
            // 更新农场的地块数量
            FarmPO farm = farmMapper.selectById(plot.getFarmId());
            if (farm != null && farm.getPlotCount() != null && farm.getPlotCount() > 0) {
                farm.setPlotCount(farm.getPlotCount() - 1);
                farmMapper.updateById(farm);
            }
            return "地块已彻底删除。";
        } else {
            return "地块删除失败。";
        }
    }

    @Autowired
    private UserReminderMapper userReminderMapper;

    @Tool("查询用户设定的定时任务。")
    @ToolName("query_reminders")
    public String queryReminders(@P("用户ID") String userId) {
        log.info("工具调用: 查询提醒, userId={}", userId);
        List<UserReminderPO> notifications = userReminderMapper.selectList(new QueryWrapper<UserReminderPO>()
                .eq("user_id", userId)
                .orderByDesc("created_at")
                .eq("status", 1)
                .last("LIMIT 20"));

        if (notifications.isEmpty()) return "暂无通知。";

        StringBuilder sb = new StringBuilder("通知列表：\n");
        for (UserReminderPO n : notifications) {
            sb.append(String.format("- ID: %d | content: %s | cron: %s | 时间: %s\n",
                    n.getId(),n.getContent(),n.getCron(),n.getCreatedAt()
                    ));
        }
        return sb.toString();
    }

}
