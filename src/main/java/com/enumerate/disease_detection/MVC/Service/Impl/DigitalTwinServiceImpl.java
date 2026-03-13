package com.enumerate.disease_detection.MVC.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.MVC.Mapper.PlotMapper;
import com.enumerate.disease_detection.MVC.Mapper.SensorDataMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.PlotPO;
import com.enumerate.disease_detection.MVC.POJO.PO.SensorDataPO;
import com.enumerate.disease_detection.MVC.Service.DigitalTwinService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DigitalTwinServiceImpl implements DigitalTwinService {

    @Autowired
    private PlotMapper plotMapper;

    @Autowired
    private SensorDataMapper sensorDataMapper;

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("tongYiModel")
    private OpenAiChatModel tongYiModel;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<PlotPO> getAllPlots() {
        return plotMapper.selectList(null);
    }

    @Override
    public SensorDataPO getPlotDetail(Long plotId) {
        return sensorDataMapper.selectOne(new QueryWrapper<SensorDataPO>()
                .eq("plot_id", plotId)
                .orderByDesc("recorded_at")
                .last("LIMIT 1"));
    }

    @Override
    public Map<String, Object> getPlotHistory(Long plotId, String type, String range) {
        LocalDateTime startTime = switch (range) {
            case "24h" -> LocalDateTime.now().minusHours(24);
            case "7d" -> LocalDateTime.now().minusDays(7);
            default -> LocalDateTime.now().minusHours(24);
        };

        List<SensorDataPO> history = sensorDataMapper.selectList(new QueryWrapper<SensorDataPO>()
                .eq("plot_id", plotId)
                .ge("recorded_at", startTime)
                .orderByAsc("recorded_at"));

        Map<String, Object> res = new HashMap<>();
        res.put("labels", history.stream().map(h -> h.getRecordedAt().toString()).collect(Collectors.toList()));
        
        List<Float> values = history.stream().map(h -> {
            return switch (type) {
                case "temp" -> h.getTemperature();
                case "humidity" -> h.getHumidity();
                case "light" -> h.getLightIntensity();
                case "moisture" -> h.getSoilMoisture();
                default -> 0f;
            };
        }).collect(Collectors.toList());
        
        res.put("values", values);
        return res;
    }

    @Override
    public void controlPlot(Long plotId, String action, Integer duration) {
        log.info("执行控制指令: 地块={}, 动作={}, 持续时间={}", plotId, action, duration);
        // 这里如果是真实硬件，应该发送 MQTT 指令
        // 模拟控制：如果是灌溉，更新 isIrrigating 状态
        if ("irrigate".equalsIgnoreCase(action)) {
            SensorDataPO latest = getPlotDetail(plotId);
            if (latest != null) {
                latest.setIsIrrigating(true);
                latest.setRecordedAt(LocalDateTime.now());
                sensorDataMapper.insert(latest);
            }
        }
    }

    // 每10分钟更新一次模拟数据 (作为测试，我先设为 30 秒)
    @Scheduled(fixedRate = 30000)
    @Override
    public void simulateDataUpdate() {
        log.info("开始执行 AI 数据模拟更新...");
        List<PlotPO> plots = plotMapper.selectList(null);
        if (plots == null || plots.isEmpty()) return;

        for (PlotPO plot : plots) {
            try {
                // 如果没有网格坐标，随机分配一个 (示例演示)
                if (plot.getGridX() == null || plot.getGridY() == null) {
                    plot.setGridX(new Random().nextInt(10));
                    plot.setGridY(new Random().nextInt(10));
                }

                SensorDataPO latest = getPlotDetail(plot.getId());
                
                String prompt = String.format(
                    "你是一个农业专家。请根据以下地块信息模拟当前的实时监测数据：\n" +
                    "地块名称: %s, 作物: %s, 生长阶段: %s, 土壤类型: %s。\n" +
                    "当前时间: %s。\n" +
                    "历史最后记录: %s。\n" +
                    "请根据农时、天气常识和作物需求，生成合理的监测数据。\n" +
                    "必须返回 JSON 格式，包含字段：temperature(℃), humidity(土壤湿度%%), npkN, npkP, npkK, lightIntensity(lux), soilMoisture(水分%%), description(简短描述)。\n" +
                    "示例格式: {\"temperature\": 24.5, \"humidity\": 65.0, \"npkN\": 12.5, \"npkP\": 8.2, \"npkK\": 10.1, \"lightIntensity\": 15000, \"soilMoisture\": 60.5, \"description\": \"环境适宜，光照充足。\"}",
                    plot.getName(), plot.getCropType(), plot.getGrowthStage(), plot.getSoilType(),
                    LocalDateTime.now(), latest != null ? latest.toString() : "无历史记录"
                );

                String aiRes = tongYiModel.chat(prompt);
                log.info("AI 模拟结果: {}", aiRes);
                
                // 清洗 AI 返回的 Markdown 代码块
                if (aiRes.contains("```json")) {
                    aiRes = aiRes.substring(aiRes.indexOf("```json") + 7, aiRes.lastIndexOf("```"));
                } else if (aiRes.contains("```")) {
                    aiRes = aiRes.substring(aiRes.indexOf("```") + 3, aiRes.lastIndexOf("```"));
                }

                JsonNode node = objectMapper.readTree(aiRes);
                
                SensorDataPO newData = SensorDataPO.builder()
                        .plotId(plot.getId())
                        .temperature(node.get("temperature").floatValue())
                        .humidity(node.get("humidity").floatValue())
                        .npkN(node.get("npkN").floatValue())
                        .npkP(node.get("npkP").floatValue())
                        .npkK(node.get("npkK").floatValue())
                        .lightIntensity(node.get("lightIntensity").floatValue())
                        .soilMoisture(node.get("soilMoisture").floatValue())
                        .isIrrigating(latest != null && latest.getIsIrrigating() != null ? latest.getIsIrrigating() : false)
                        .description(node.get("description").asText())
                        .recordedAt(LocalDateTime.now())
                        .build();

                sensorDataMapper.insert(newData);

                // 更新地块健康分和状态
                int score = calculateHealthScore(newData);
                plot.setHealthScore(score);
                plot.setStatus(determineStatus(score));
                plotMapper.updateById(plot);

            } catch (Exception e) {
                log.error("地块 {} 模拟数据失败", plot.getId(), e);
            }
        }
    }

    private int calculateHealthScore(SensorDataPO data) {
        int score = 100;
        // 简单算法：根据阈值扣分
        if (data.getTemperature() > 35 || data.getTemperature() < 5) score -= 15;
        if (data.getSoilMoisture() < 30) score -= 20;
        if (data.getSoilMoisture() > 90) score -= 10;
        if (data.getHumidity() < 40) score -= 5;
        return Math.max(0, score);
    }

    private String determineStatus(int score) {
        if (score >= 90) return "healthy";
        if (score >= 70) return "normal";
        if (score >= 50) return "warning";
        return "danger";
    }
}
