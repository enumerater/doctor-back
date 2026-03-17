//package com.enumerate.disease_detection.MVC.Controller;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.enumerate.disease_detection.Common.Result;
//import com.enumerate.disease_detection.Local.UserContextHolder;
//import com.enumerate.disease_detection.MVC.Mapper.FarmMapper;
//import com.enumerate.disease_detection.MVC.Mapper.PlotMapper;
//import com.enumerate.disease_detection.MVC.Mapper.PlotStageMapper;
//import com.enumerate.disease_detection.MVC.POJO.PO.FarmPO;
//import com.enumerate.disease_detection.MVC.POJO.PO.PlotPO;
//import com.enumerate.disease_detection.MVC.POJO.PO.PlotStagePO;
//import com.enumerate.disease_detection.MVC.Service.WeatherService;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//import java.time.temporal.ChronoUnit;
//import java.util.*;
//
//@RestController
//@RequestMapping("/farm")
//@CrossOrigin
//@Slf4j
//public class FarmTwinController {
//
//    @Autowired
//    private FarmMapper farmMapper;
//
//    @Autowired
//    private PlotMapper plotMapper;
//
//    @Autowired
//    private PlotStageMapper plotStageMapper;
//
//    @Autowired
//    private WeatherService weatherService;
//
//    // TODO: 可注入 WeatherService 用于获取当地真实基础气温
//
//    /**
//     * 1. 获取数字孪生农场布局数据 (算法推算 3D 坐标与尺寸)
//     */
//    @GetMapping("/{farmId}/twin/layout")
//    public Result<Map<String, Object>> getTwinLayout(@PathVariable String farmId) {
//        log.info("【AI推算】获取农场数字孪生布局：{}", farmId);
//        FarmPO farmPO = farmMapper.selectById(farmId);
//        if (farmPO == null) {
//            return Result.error(404, "农场不存在");
//        }
//
//        List<PlotPO> plotList = plotMapper.selectList(new QueryWrapper<PlotPO>().eq("farm_id", farmId));
//        int count = plotList.size();
//
//        // 1. 网格布局算法 (推算3D坐标)
//        int cols = (int) Math.ceil(Math.sqrt(count));
//        double spacing = 55.0; // 地块间距
//        double totalWidth = (cols - 1) * spacing;
//        double startX = -totalWidth / 2.0;
//
//        List<Map<String, Object>> twinPlots = new ArrayList<>();
//        double maxX = 80.0, maxZ = 60.0;
//
//        for (int i = 0; i < count; i++) {
//            PlotPO plot = plotList.get(i);
//
//            // 坐标推算
//            int col = i % cols;
//            int row = i / cols;
//            int rows = (int) Math.ceil((double) count / cols);
//            double totalDepth = (rows - 1) * spacing;
//            double startZ = -totalDepth / 2.0;
//
//            double x = startX + col * spacing;
//            double z = startZ + row * spacing;
//
//            // 尺寸推算 (1亩≈667㎡，按面积比例计算3D尺寸)
//            double area = plot.getArea() != null ? plot.getArea() : 1.0;
//            double scale = Math.sqrt(area) * 12;
//            double width = Math.max(20, scale * 1.2);
//            double depth = Math.max(16, scale);
//
//            // 更新地面总大小
//            maxX = Math.max(maxX, Math.abs(x) + width / 2 + 20);
//            maxZ = Math.max(maxZ, Math.abs(z) + depth / 2 + 20);
//
//            // 健康度推算 (根据播种天数和阶段记录计算)
//            int healthScore = calculateHealthScore(String.valueOf(plot.getId()), plot.getSowingDate(), plot.getGrowthStage());
//
//            // 组装单个地块孪生数据
//            Map<String, Object> twinPlot = new HashMap<>();
//            twinPlot.put("plotId", String.valueOf(plot.getId()));
//            twinPlot.put("plotName", plot.getName());
//            twinPlot.put("cropType", plot.getCropType());
//            twinPlot.put("growthStage", plot.getGrowthStage() != null ? plot.getGrowthStage() : "未知");
//            twinPlot.put("healthScore", healthScore);
//            twinPlot.put("position", Map.of("x", x, "z", z));
//            twinPlot.put("size", Map.of("width", width, "depth", depth));
//            twinPlot.put("area", area);
//            twinPlot.put("soilType", plot.getSoilType());
//            twinPlot.put("sowingDate", plot.getSowingDate());
//            twinPlots.add(twinPlot);
//        }
//
//        // 组装最终返回数据
//        Map<String, Object> data = new HashMap<>();
//        data.put("farmId", String.valueOf(farmPO.getId()));
//        data.put("farmName", farmPO.getName());
//        data.put("farmLocation", farmPO.getLocation());
//        data.put("groundSize", Map.of("width", maxX * 2, "depth", maxZ * 2));
//        data.put("plots", twinPlots);
//
//        return Result.success(data);
//    }
//
//    /**
//     * 2. 获取数字孪生地块物联网数据 (AI大模型/算法 模拟环境数据)
//     */
//    @GetMapping("/{farmId}/twin/iot")
//    public Result<Map<String, Object>> getTwinIoTData(@PathVariable String farmId) {
//        log.info("【AI推算】基于真实天气生成地块孪生数据：{}", farmId);
//
//        // 1. 获取农场信息以拿到地址
//        FarmPO farmPO = farmMapper.selectById(farmId);
//        String location = farmPO != null ? farmPO.getLocation() : "北京市";
//
//        // 2. 调用天气服务获取当地【真实的】基准温度和湿度
//        Map<String, Double> realWeather = weatherService.getCurrentTempAndHum(location);
//        double baseTemp = realWeather.get("temp");
//        double baseHum = realWeather.get("humidity");
//
//        log.info("【AI推算】获取当地真实天气：{} || {}", baseTemp, baseHum);
//
//        List<PlotPO> plotList = plotMapper.selectList(new QueryWrapper<PlotPO>().eq("farm_id", farmId));
//        Random random = new Random();
//        Map<String, Object> iotDataResult = new HashMap<>();
//
//        for (PlotPO plot : plotList) {
//            Map<String, Object> plotIot = new HashMap<>();
//            plotIot.put("plotId", String.valueOf(plot.getId()));
//
//            Map<String, Object> sensors = new HashMap<>();
//
//            // 基于真实天气，给每个地块增加微小的环境扰动 (例如同一农场不同地块温度相差 ±0.5℃，湿度相差 ±2%)
//            double plotTemp = baseTemp + (random.nextDouble() - 0.5);
//            double plotHum = baseHum + (random.nextDouble() - 0.5) * 4;
//
//            sensors.put("temperature", buildSensor(plotTemp, "°C", -10, 45, "气温"));
//            sensors.put("humidity", buildSensor(plotHum, "%", 0, 100, "湿度"));
//
//            // 其他光照、水肥数据依然可以使用 AI 算法随机推算
//            sensors.put("light", buildSensor(25000 + random.nextInt(15000), "lux", 0, 80000, "光照"));
//            sensors.put("soilMoisture", buildSensor(30 + random.nextInt(30), "%", 0, 100, "土壤水分"));
//            sensors.put("soilFertility", buildSensor(50 + random.nextInt(30), "", 0, 100, "土壤肥力"));
//
//            plotIot.put("sensors", sensors);
//            iotDataResult.put(String.valueOf(plot.getId()), plotIot);
//        }
//
//        return Result.success(iotDataResult);
//    }
//
//    // ================= 辅助算法方法 =================
//
//    /**
//     * AI推算作物的健康评分
//     */
//    private int calculateHealthScore(String plotId, String sowingDateStr, String growthStage) {
//        if (sowingDateStr == null || "休耕".equals(growthStage)) {
//            return 45;
//        }
//        try {
//            LocalDate sowingDate = LocalDate.parse(sowingDateStr);
//            long days = ChronoUnit.DAYS.between(sowingDate, LocalDate.now());
//
//            // 获取生长阶段历史记录数量作为加分项
//            long stageCount = plotStageMapper.selectCount(new QueryWrapper<PlotStagePO>().eq("plot_id", plotId));
//
//            int score = 60 + (int) stageCount * 5;
//            if (days > 0 && days < 300) {
//                score += 10;
//            }
//            return Math.min(100, Math.max(0, score));
//        } catch (Exception e) {
//            return 60; // 日期解析失败默认及格
//        }
//    }
//
//    /**
//     * 构建单个传感器的标准数据格式
//     */
//    private Map<String, Object> buildSensor(double value, String unit, int min, int max, String label) {
//        Map<String, Object> sensor = new HashMap<>();
//        // 保留一位小数
//        sensor.put("value", Math.round(value * 10.0) / 10.0);
//        sensor.put("unit", unit);
//        sensor.put("min", min);
//        sensor.put("max", max);
//        sensor.put("label", label);
//        return sensor;
//    }
//}


package com.enumerate.disease_detection.MVC.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.MVC.Mapper.FarmMapper;
import com.enumerate.disease_detection.MVC.Mapper.PlotMapper;
import com.enumerate.disease_detection.MVC.Mapper.PlotStageMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.FarmPO;
import com.enumerate.disease_detection.MVC.POJO.PO.PlotPO;
import com.enumerate.disease_detection.MVC.POJO.PO.PlotStagePO;
import com.enumerate.disease_detection.MVC.Service.WeatherService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/farm")
@CrossOrigin
@Slf4j
public class FarmTwinController {

    @Autowired
    private FarmMapper farmMapper;

    @Autowired
    private PlotMapper plotMapper;

    @Autowired
    private PlotStageMapper plotStageMapper;

    @Autowired
    private WeatherService weatherService;

    // 1. 注入大模型 (使用你在 MainModel 中配置的同步模型)
    @Resource(name = "tongYiModel")
    private OpenAiChatModel chatModel;

    // 2. 注入 JSON 解析工具
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 1. 获取数字孪生农场布局数据 (保持不变)
     */
    @GetMapping("/{farmId}/twin/layout")
    public Result<Map<String, Object>> getTwinLayout(@PathVariable String farmId) {
        // ... (保持你原有的逻辑不变)
        log.info("【AI推算】获取农场数字孪生布局：{}", farmId);
        FarmPO farmPO = farmMapper.selectById(farmId);
        if (farmPO == null) {
            return Result.error(404, "农场不存在");
        }

        List<PlotPO> plotList = plotMapper.selectList(new QueryWrapper<PlotPO>().eq("farm_id", farmId));
        int count = plotList.size();

        int cols = (int) Math.ceil(Math.sqrt(count));
        double spacing = 55.0;
        double totalWidth = (cols - 1) * spacing;
        double startX = -totalWidth / 2.0;

        List<Map<String, Object>> twinPlots = new ArrayList<>();
        double maxX = 80.0, maxZ = 60.0;

        for (int i = 0; i < count; i++) {
            PlotPO plot = plotList.get(i);
            int col = i % cols;
            int row = i / cols;
            int rows = (int) Math.ceil((double) count / cols);
            double totalDepth = (rows - 1) * spacing;
            double startZ = -totalDepth / 2.0;

            double x = startX + col * spacing;
            double z = startZ + row * spacing;

            double area = plot.getArea() != null ? plot.getArea() : 1.0;
            double scale = Math.sqrt(area) * 12;
            double width = Math.max(20, scale * 1.2);
            double depth = Math.max(16, scale);

            maxX = Math.max(maxX, Math.abs(x) + width / 2 + 20);
            maxZ = Math.max(maxZ, Math.abs(z) + depth / 2 + 20);

            int healthScore = calculateHealthScore(String.valueOf(plot.getId()), plot.getSowingDate(), plot.getGrowthStage());

            Map<String, Object> twinPlot = new HashMap<>();
            twinPlot.put("plotId", String.valueOf(plot.getId()));
            twinPlot.put("plotName", plot.getName());
            twinPlot.put("cropType", plot.getCropType());
            twinPlot.put("growthStage", plot.getGrowthStage() != null ? plot.getGrowthStage() : "未知");
            twinPlot.put("healthScore", healthScore);
            twinPlot.put("position", Map.of("x", x, "z", z));
            twinPlot.put("size", Map.of("width", width, "depth", depth));
            twinPlot.put("area", area);
            twinPlot.put("soilType", plot.getSoilType());
            twinPlot.put("sowingDate", plot.getSowingDate());
            twinPlots.add(twinPlot);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("farmId", String.valueOf(farmPO.getId()));
        data.put("farmName", farmPO.getName());
        data.put("farmLocation", farmPO.getLocation());
        data.put("groundSize", Map.of("width", maxX * 2, "depth", maxZ * 2));
        data.put("plots", twinPlots);

        return Result.success(data);
    }

    /**
     * 2. 获取数字孪生地块物联网数据 (结合大模型进行深度推算)
     */
    @GetMapping("/{farmId}/twin/iot")
    public Result<Map<String, Object>> getTwinIoTData(@PathVariable String farmId) {
        log.info("【AI推算】基于真实天气和大模型推理生成地块孪生数据：{}", farmId);

        FarmPO farmPO = farmMapper.selectById(farmId);
        String location = farmPO != null ? farmPO.getLocation() : "未知地区";

        // 获取当地真实天气作为大模型的推理基准
        Map<String, Double> realWeather = weatherService.getCurrentTempAndHum(location);
        double baseTemp = realWeather.get("temp");
        double baseHum = realWeather.get("humidity");

        List<PlotPO> plotList = plotMapper.selectList(new QueryWrapper<PlotPO>().eq("farm_id", farmId));
        Map<String, Object> iotDataResult = new HashMap<>();
        Random random = new Random();

        // 3. 尝试使用大模型推理光照、水分和肥力
        Map<String, Map<String, Integer>> aiInferredData = getAiInferredSensors(location, baseTemp, baseHum, plotList);

        for (PlotPO plot : plotList) {
            String pId = String.valueOf(plot.getId());
            Map<String, Object> plotIot = new HashMap<>();
            plotIot.put("plotId", pId);

            Map<String, Object> sensors = new HashMap<>();

            // 真实天气加微小扰动
            double plotTemp = baseTemp + (random.nextDouble() - 0.5);
            double plotHum = baseHum + (random.nextDouble() - 0.5) * 4;
            sensors.put("temperature", buildSensor(plotTemp, "°C", -10, 45, "气温"));
            sensors.put("humidity", buildSensor(plotHum, "%", 0, 100, "湿度"));

            // 使用AI推理的数据，如果AI失败或超时，则使用兜底逻辑(随机数)
            int lightVal = 25000 + random.nextInt(15000);
            int moistureVal = 30 + random.nextInt(30);
            int fertilityVal = 50 + random.nextInt(30);

            if (aiInferredData != null && aiInferredData.containsKey(pId)) {
                Map<String, Integer> inferred = aiInferredData.get(pId);
                lightVal = inferred.getOrDefault("light", lightVal);
                moistureVal = inferred.getOrDefault("soilMoisture", moistureVal);
                fertilityVal = inferred.getOrDefault("soilFertility", fertilityVal);
            }

            sensors.put("light", buildSensor(lightVal, "lux", 0, 80000, "光照"));
            sensors.put("soilMoisture", buildSensor(moistureVal, "%", 0, 100, "土壤水分"));
            sensors.put("soilFertility", buildSensor(fertilityVal, "", 0, 100, "土壤肥力"));

            plotIot.put("sensors", sensors);
            iotDataResult.put(pId, plotIot);
        }

        return Result.success(iotDataResult);
    }

    /**
     * 核心逻辑：调用大模型批量推算物联网数据
     */
    private Map<String, Map<String, Integer>> getAiInferredSensors(String location, double baseTemp, double baseHum, List<PlotPO> plotList) {
        if (plotList == null || plotList.isEmpty()) return null;

        try {
            // 1. 组装地块简要信息给 AI
            List<Map<String, String>> plotInfos = new ArrayList<>();
            for (PlotPO plot : plotList) {
                Map<String, String> info = new HashMap<>();
                info.put("plotId", String.valueOf(plot.getId()));
                info.put("cropType", plot.getCropType() != null ? plot.getCropType() : "未知");
                info.put("growthStage", plot.getGrowthStage() != null ? plot.getGrowthStage() : "未知");
                info.put("soilType", plot.getSoilType() != null ? plot.getSoilType() : "未知");
                plotInfos.add(info);

                log.info("【AI推理】地块信息：{}", info);
            }
            String plotJson = objectMapper.writeValueAsString(plotInfos);

            // 2. 构建严谨的 Prompt，要求 AI 只返回 JSON
            String prompt = String.format("""
                你是一个农业数字孪生系统的物理引擎大模型。请根据当前的真实环境和地块的作物信息，科学地推算出当前时刻合理的农田传感器数据。
                
                【当前大环境】
                地点：%s
                环境温度：%.1f ℃
                环境湿度：%.1f %%
                
                【地块详情列表】
                %s
                
                【推理规则】
                1. 光照(light)：考虑温度和一般规律（温度高一般代表白天光照强），范围 0-80000 lux。
                2. 土壤水分(soilMoisture)：受作物种类、生长阶段影响（如水稻水分极高，旱地作物较低），范围 0-100 %%。
                3. 土壤肥力(soilFertility)：苗期/生长期对肥力消耗不同，结合土壤类型推断，范围 0-100。
                
                【输出要求】
                请严格按照以下JSON格式输出，不要包含任何Markdown代码块标记（如```json），只输出合法的JSON对象：
                {
                  "地块ID": { "light": 45000, "soilMoisture": 60, "soilFertility": 75 }
                }
                """, location, baseTemp, baseHum, plotJson);

            // 3. 调用 LangChain4j ChatModel
            ChatRequest request = ChatRequest.builder()
                    .messages(UserMessage.from(prompt))
                    .build();
            ChatResponse response = chatModel.chat(request);
            String aiResult = response.aiMessage().text();

            // 4. 清理可能携带的 Markdown 标记
            aiResult = aiResult.replaceAll("```json", "").replaceAll("```", "").trim();

            // 5. 解析并返回
            return objectMapper.readValue(aiResult, new TypeReference<Map<String, Map<String, Integer>>>() {});

        } catch (Exception e) {
            log.error("调用大模型推算地块数据失败，将使用默认随机逻辑", e);
            return null; // 失败返回 null，外层会自动走兜底逻辑
        }
    }

    private int calculateHealthScore(String plotId, String sowingDateStr, String growthStage) {
        if (sowingDateStr == null || "休耕".equals(growthStage)) {
            return 45;
        }
        try {
            LocalDate sowingDate = LocalDate.parse(sowingDateStr);
            long days = ChronoUnit.DAYS.between(sowingDate, LocalDate.now());
            long stageCount = plotStageMapper.selectCount(new QueryWrapper<PlotStagePO>().eq("plot_id", plotId));
            int score = 60 + (int) stageCount * 5;
            if (days > 0 && days < 300) {
                score += 10;
            }
            return Math.min(100, Math.max(0, score));
        } catch (Exception e) {
            return 60;
        }
    }

    private Map<String, Object> buildSensor(double value, String unit, int min, int max, String label) {
        Map<String, Object> sensor = new HashMap<>();
        sensor.put("value", Math.round(value * 10.0) / 10.0);
        sensor.put("unit", unit);
        sensor.put("min", min);
        sensor.put("max", max);
        sensor.put("label", label);
        return sensor;
    }
}