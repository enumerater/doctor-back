package com.enumerate.disease_detection.Service;

import com.enumerate.disease_detection.POJO.VO.dashboard.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DashboardService {

    // ==================== 接口1: 地区树 ====================
    public List<RegionVO> getRegions() {
        List<RegionVO> provinces = new ArrayList<>();

        provinces.add(buildProvince("110000", "北京市", 116.40, 39.90, List.of(
                buildCity("110100", "北京市区", 116.40, 39.90, List.of(
                        buildDistrict("110101", "东城区", 116.42, 39.93),
                        buildDistrict("110102", "西城区", 116.37, 39.91),
                        buildDistrict("110105", "朝阳区", 116.49, 39.92),
                        buildDistrict("110106", "丰台区", 116.29, 39.86),
                        buildDistrict("110108", "海淀区", 116.30, 39.96)
                ))
        )));

        provinces.add(buildProvince("310000", "上海市", 121.47, 31.23, List.of(
                buildCity("310100", "上海市区", 121.47, 31.23, List.of(
                        buildDistrict("310101", "黄浦区", 121.49, 31.23),
                        buildDistrict("310104", "徐汇区", 121.44, 31.19),
                        buildDistrict("310105", "长宁区", 121.42, 31.22),
                        buildDistrict("310115", "浦东新区", 121.54, 31.22)
                ))
        )));

        provinces.add(buildProvince("330000", "浙江省", 120.15, 30.26, List.of(
                buildCity("330100", "杭州市", 120.15, 30.26, List.of(
                        buildDistrict("330102", "上城区", 120.17, 30.25),
                        buildDistrict("330105", "拱墅区", 120.14, 30.32),
                        buildDistrict("330106", "西湖区", 120.13, 30.26)
                )),
                buildCity("330200", "宁波市", 121.55, 29.87, List.of(
                        buildDistrict("330203", "海曙区", 121.55, 29.86),
                        buildDistrict("330205", "江北区", 121.56, 29.89)
                ))
        )));

        provinces.add(buildProvince("320000", "江苏省", 118.76, 32.04, List.of(
                buildCity("320100", "南京市", 118.76, 32.04, List.of(
                        buildDistrict("320102", "玄武区", 118.80, 32.05),
                        buildDistrict("320104", "秦淮区", 118.79, 32.02)
                )),
                buildCity("320500", "苏州市", 120.62, 31.30, List.of(
                        buildDistrict("320505", "虎丘区", 120.57, 31.30),
                        buildDistrict("320506", "吴中区", 120.63, 31.26)
                ))
        )));

        provinces.add(buildProvince("440000", "广东省", 113.26, 23.13, List.of(
                buildCity("440100", "广州市", 113.26, 23.13, List.of(
                        buildDistrict("440103", "荔湾区", 113.24, 23.13),
                        buildDistrict("440104", "越秀区", 113.27, 23.13),
                        buildDistrict("440106", "天河区", 113.36, 23.12)
                )),
                buildCity("440300", "深圳市", 114.06, 22.55, List.of(
                        buildDistrict("440303", "罗湖区", 114.13, 22.55),
                        buildDistrict("440304", "福田区", 114.06, 22.52),
                        buildDistrict("440305", "南山区", 113.93, 22.53)
                ))
        )));

        provinces.add(buildProvince("510000", "四川省", 104.07, 30.67, List.of(
                buildCity("510100", "成都市", 104.07, 30.67, List.of(
                        buildDistrict("510104", "锦江区", 104.08, 30.66),
                        buildDistrict("510105", "青羊区", 104.06, 30.67),
                        buildDistrict("510107", "武侯区", 104.04, 30.64)
                ))
        )));

        provinces.add(buildProvince("420000", "湖北省", 114.34, 30.55, List.of(
                buildCity("420100", "武汉市", 114.34, 30.55, List.of(
                        buildDistrict("420102", "江岸区", 114.31, 30.60),
                        buildDistrict("420103", "江汉区", 114.27, 30.60),
                        buildDistrict("420104", "硚口区", 114.26, 30.57)
                ))
        )));

        provinces.add(buildProvince("370000", "山东省", 117.00, 36.67, List.of(
                buildCity("370100", "济南市", 117.00, 36.67, List.of(
                        buildDistrict("370102", "历下区", 117.08, 36.67),
                        buildDistrict("370103", "市中区", 116.99, 36.65)
                )),
                buildCity("370200", "青岛市", 120.38, 36.07, List.of(
                        buildDistrict("370202", "市南区", 120.39, 36.07),
                        buildDistrict("370203", "市北区", 120.37, 36.09)
                ))
        )));

        return provinces;
    }

    private RegionVO buildProvince(String code, String name, double lng, double lat, List<RegionVO> children) {
        return RegionVO.builder().code(code).name(name).center(new Double[]{lng, lat}).children(children).build();
    }

    private RegionVO buildCity(String code, String name, double lng, double lat, List<RegionVO> children) {
        return RegionVO.builder().code(code).name(name).center(new Double[]{lng, lat}).children(children).build();
    }

    private RegionVO buildDistrict(String code, String name, double lng, double lat) {
        return RegionVO.builder().code(code).name(name).center(new Double[]{lng, lat}).build();
    }

    // ==================== 接口2: KPI 汇总 ====================
    public RegionSummaryVO getRegionSummary(String regionCode, String timeRange) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int base = regionCode.equals("100000") ? 5 : 1;
        return RegionSummaryVO.builder()
                .totalDiagnosis(base * r.nextInt(10000, 20000))
                .todayDiagnosis(base * r.nextInt(100, 300))
                .diseaseTypes(r.nextInt(30, 60))
                .monitorStations(base * r.nextInt(200, 500))
                .aiAccuracy(Math.round(r.nextDouble(93.0, 99.0) * 10.0) / 10.0)
                .alertCount(r.nextInt(5, 30))
                .build();
    }

    // ==================== 接口3: 地图标记点 ====================
    public List<MapMarkerVO> getMapMarkers(String regionCode, String types, String crops) {
        Set<String> typeFilter = (types == null || types.isEmpty())
                ? Set.of("disease", "crop", "weather")
                : Set.of(types.split(","));
        Set<String> cropFilter = (crops == null || crops.isEmpty())
                ? null
                : Set.of(crops.split(","));

        List<MapMarkerVO> all = new ArrayList<>();
        int id = 1;

        // 病害标记
        if (typeFilter.contains("disease")) {
            String[][] diseases = {
                    {"番茄早疫病", "番茄"}, {"番茄晚疫病", "番茄"}, {"黄瓜霜霉病", "黄瓜"},
                    {"辣椒疫病", "辣椒"}, {"草莓灰霉病", "草莓"}, {"水稻稻瘟病", "水稻"},
                    {"小麦锈病", "小麦"}, {"玉米大斑病", "玉米"}, {"苹果腐烂病", "苹果"},
                    {"葡萄霜霉病", "葡萄"}
            };
            double[][] coords = {
                    {116.42, 39.93}, {116.35, 39.88}, {121.50, 31.25},
                    {120.18, 30.28}, {113.28, 23.15}, {104.10, 30.70},
                    {114.36, 30.58}, {117.02, 36.69}, {120.40, 36.08},
                    {118.78, 32.06}
            };
            for (int i = 0; i < diseases.length; i++) {
                if (cropFilter != null && !cropFilter.contains(diseases[i][1])) continue;
                int severity = ThreadLocalRandom.current().nextInt(1, 4);
                int area = ThreadLocalRandom.current().nextInt(30, 200);
                String[] severityText = {"", "轻微", "中等", "严重"};
                all.add(MapMarkerVO.builder()
                        .id(id++).type("disease")
                        .lng(coords[i][0]).lat(coords[i][1])
                        .name(diseases[i][0])
                        .description("严重程度: " + severityText[severity] + " | 面积: " + area + "亩")
                        .severity(severity)
                        .crop(diseases[i][1])
                        .build());
            }
        }

        // 作物基地标记
        if (typeFilter.contains("crop")) {
            String[] cropNames = {"番茄", "黄瓜", "辣椒", "草莓", "水稻", "小麦"};
            double[][] cropCoords = {
                    {116.60, 39.80}, {121.60, 31.15}, {120.30, 30.20},
                    {113.40, 23.08}, {104.20, 30.60}, {114.50, 30.50}
            };
            for (int i = 0; i < cropNames.length; i++) {
                if (cropFilter != null && !cropFilter.contains(cropNames[i])) continue;
                int plantArea = ThreadLocalRandom.current().nextInt(500, 5000);
                int yield = plantArea * ThreadLocalRandom.current().nextInt(2, 5);
                all.add(MapMarkerVO.builder()
                        .id(id++).type("crop")
                        .lng(cropCoords[i][0]).lat(cropCoords[i][1])
                        .name(cropNames[i] + "种植基地")
                        .description("种植面积: " + plantArea + "亩 | 产量: " + yield + "吨")
                        .severity(0)
                        .crop(cropNames[i])
                        .build());
            }
        }

        // 气象站标记
        if (typeFilter.contains("weather")) {
            double[][] weatherCoords = {
                    {116.30, 39.95}, {121.45, 31.20}, {120.10, 30.30},
                    {113.20, 23.10}, {117.10, 36.70}
            };
            for (int i = 0; i < weatherCoords.length; i++) {
                double temp = Math.round(ThreadLocalRandom.current().nextDouble(15.0, 30.0) * 10.0) / 10.0;
                int humidity = ThreadLocalRandom.current().nextInt(40, 85);
                all.add(MapMarkerVO.builder()
                        .id(id++).type("weather")
                        .lng(weatherCoords[i][0]).lat(weatherCoords[i][1])
                        .name("气象监测站 #" + (i + 1))
                        .description("温度: " + temp + "°C | 湿度: " + humidity + "%")
                        .severity(0)
                        .crop(null)
                        .build());
            }
        }

        return all;
    }

    // ==================== 接口4: 病害预警列表 ====================
    public List<DiseaseAlertVO> getDiseaseAlerts(String regionCode, Integer limit) {
        if (limit == null) limit = 20;

        String[][] alertData = {
                {"番茄早疫病", "北京朝阳区", "116.49", "39.92"},
                {"黄瓜霜霉病", "上海浦东新区", "121.54", "31.22"},
                {"水稻稻瘟病", "杭州西湖区", "120.13", "30.26"},
                {"小麦锈病", "南京玄武区", "118.80", "32.05"},
                {"辣椒疫病", "广州天河区", "113.36", "23.12"},
                {"草莓灰霉病", "成都锦江区", "104.08", "30.66"},
                {"玉米大斑病", "武汉江岸区", "114.31", "30.60"},
                {"苹果腐烂病", "济南历下区", "117.08", "36.67"},
                {"葡萄霜霉病", "苏州虎丘区", "120.57", "31.30"},
                {"番茄晚疫病", "深圳南山区", "113.93", "22.53"},
                {"水稻纹枯病", "宁波海曙区", "121.55", "29.86"},
                {"小麦白粉病", "青岛市南区", "120.39", "36.07"},
                {"番茄灰霉病", "北京海淀区", "116.30", "39.96"},
                {"黄瓜枯萎病", "上海黄浦区", "121.49", "31.23"},
                {"辣椒炭疽病", "广州荔湾区", "113.24", "23.13"},
        };
        String[] levels = {"low", "medium", "high", "critical"};
        String[] trends = {"up", "down", "stable"};

        List<DiseaseAlertVO> alerts = new ArrayList<>();
        ThreadLocalRandom r = ThreadLocalRandom.current();

        int count = Math.min(limit, alertData.length);
        for (int i = 0; i < count; i++) {
            int hour = r.nextInt(6, 23);
            int minute = r.nextInt(0, 60);
            alerts.add(DiseaseAlertVO.builder()
                    .id(i + 1)
                    .disease(alertData[i][0])
                    .location(alertData[i][1])
                    .level(levels[r.nextInt(levels.length)])
                    .area(r.nextInt(20, 300))
                    .time(String.format("%02d:%02d", hour, minute))
                    .lng(Double.parseDouble(alertData[i][2]))
                    .lat(Double.parseDouble(alertData[i][3]))
                    .trend(trends[r.nextInt(trends.length)])
                    .build());
        }

        // 按时间倒序
        alerts.sort((a, b) -> b.getTime().compareTo(a.getTime()));
        return alerts;
    }

    // ==================== 接口5: 诊断趋势 ====================
    public List<DiagnosisTrendVO> getDiagnosisTrend(String regionCode, String timeRange, String crops) {
        List<DiagnosisTrendVO> list = new ArrayList<>();
        ThreadLocalRandom r = ThreadLocalRandom.current();
        LocalDate today = LocalDate.now();

        int points;
        switch (timeRange) {
            case "today":
                points = 24;
                for (int i = 0; i < points; i++) {
                    int count = r.nextInt(5, 30);
                    list.add(DiagnosisTrendVO.builder()
                            .date(i + ":00")
                            .count(count)
                            .aiCount((int) (count * r.nextDouble(0.5, 0.85)))
                            .build());
                }
                break;
            case "week":
                points = 7;
                for (int i = points - 1; i >= 0; i--) {
                    LocalDate d = today.minusDays(i);
                    int count = r.nextInt(100, 250);
                    list.add(DiagnosisTrendVO.builder()
                            .date(d.getMonthValue() + "/" + d.getDayOfMonth())
                            .count(count)
                            .aiCount((int) (count * r.nextDouble(0.6, 0.85)))
                            .build());
                }
                break;
            case "quarter":
                points = 90;
                for (int i = points - 1; i >= 0; i--) {
                    LocalDate d = today.minusDays(i);
                    int count = r.nextInt(80, 300);
                    list.add(DiagnosisTrendVO.builder()
                            .date(d.getMonthValue() + "/" + d.getDayOfMonth())
                            .count(count)
                            .aiCount((int) (count * r.nextDouble(0.6, 0.85)))
                            .build());
                }
                break;
            case "year":
                points = 12;
                for (int i = points - 1; i >= 0; i--) {
                    LocalDate d = today.minusMonths(i);
                    int count = r.nextInt(2000, 6000);
                    list.add(DiagnosisTrendVO.builder()
                            .date(d.getMonthValue() + "月")
                            .count(count)
                            .aiCount((int) (count * r.nextDouble(0.6, 0.85)))
                            .build());
                }
                break;
            default: // month
                points = 30;
                for (int i = points - 1; i >= 0; i--) {
                    LocalDate d = today.minusDays(i);
                    int count = r.nextInt(100, 250);
                    list.add(DiagnosisTrendVO.builder()
                            .date(d.getMonthValue() + "/" + d.getDayOfMonth())
                            .count(count)
                            .aiCount((int) (count * r.nextDouble(0.6, 0.85)))
                            .build());
                }
                break;
        }
        return list;
    }

    // ==================== 接口6: 作物种类分布 ====================
    public List<CropDistributionVO> getCropDistribution(String regionCode) {
        List<CropDistributionVO> list = new ArrayList<>();
        String[] names = {"水稻", "小麦", "番茄", "黄瓜", "玉米", "辣椒", "草莓", "白菜", "苹果", "葡萄"};
        int[] baseValues = {4500, 3800, 3200, 2800, 2600, 2100, 1500, 1200, 980, 760};

        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < names.length; i++) {
            int offset = r.nextInt(-200, 200);
            list.add(CropDistributionVO.builder()
                    .name(names[i])
                    .value(Math.max(100, baseValues[i] + offset))
                    .build());
        }
        return list;
    }

    // ==================== 接口7: 天气数据 ====================
    public WeatherVO getWeather(String regionCode, String timeRange) {
        ThreadLocalRandom r = ThreadLocalRandom.current();

        String[] weathers = {"晴", "多云", "阴", "雨", "雪", "雾"};
        String[] icons = {"sunny", "cloudy", "overcast", "rain", "snow", "fog"};
        String[] winds = {"东南风 3级", "南风 2级", "西北风 4级", "东风 1级", "北风 3级"};

        int wIdx = r.nextInt(weathers.length);
        WeatherVO.CurrentWeather current = WeatherVO.CurrentWeather.builder()
                .temp(Math.round(r.nextDouble(10.0, 28.0) * 10.0) / 10.0)
                .humidity(r.nextInt(40, 80))
                .wind(winds[r.nextInt(winds.length)])
                .weather(weathers[wIdx])
                .icon(icons[wIdx])
                .aqi(r.nextInt(30, 120))
                .build();

        List<WeatherVO.ForecastItem> forecast = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 1; i <= 5; i++) {
            LocalDate d = today.plusDays(i);
            int fIdx = r.nextInt(weathers.length);
            double high = Math.round(r.nextDouble(18.0, 32.0) * 10.0) / 10.0;
            double low = Math.round(r.nextDouble(5.0, high - 5) * 10.0) / 10.0;
            forecast.add(WeatherVO.ForecastItem.builder()
                    .date(d.getMonthValue() + "/" + d.getDayOfMonth())
                    .weather(weathers[fIdx])
                    .icon(icons[fIdx])
                    .high(high)
                    .low(low)
                    .build());
        }

        return WeatherVO.builder().current(current).forecast(forecast).build();
    }

    // ==================== 接口8: 农产品价格趋势 ====================
    public List<PriceIndexVO> getPriceIndex(String crop, String timeRange) {
        Map<String, double[]> priceData = new LinkedHashMap<>();
        priceData.put("番茄", new double[]{5.20, 4.80, 5.50, 6.10, 5.90, 5.30, 4.90, 5.60, 6.20, 5.80, 5.40, 5.10});
        priceData.put("黄瓜", new double[]{3.50, 3.20, 3.80, 4.10, 3.90, 3.60, 3.30, 3.70, 4.00, 3.80, 3.50, 3.20});
        priceData.put("辣椒", new double[]{8.00, 7.50, 8.20, 9.10, 8.80, 8.30, 7.90, 8.50, 9.00, 8.60, 8.10, 7.80});
        priceData.put("草莓", new double[]{15.00, 14.50, 16.00, 18.20, 17.50, 15.80, 14.00, 15.50, 17.00, 16.50, 15.20, 14.80});
        priceData.put("水稻", new double[]{2.80, 2.75, 2.90, 3.00, 2.95, 2.85, 2.78, 2.88, 2.98, 2.92, 2.82, 2.76});
        priceData.put("小麦", new double[]{2.50, 2.45, 2.60, 2.70, 2.65, 2.55, 2.48, 2.58, 2.68, 2.62, 2.52, 2.46});

        List<PriceIndexVO> result = new ArrayList<>();
        ThreadLocalRandom r = ThreadLocalRandom.current();

        if (crop != null && !crop.isEmpty()) {
            double[] base = priceData.getOrDefault(crop, new double[]{3.0, 3.1, 3.2, 3.0, 2.9, 3.1, 3.3, 3.2, 3.0, 2.8, 3.1, 3.2});
            List<Double> data = new ArrayList<>();
            for (double v : base) {
                data.add(Math.round((v + r.nextDouble(-0.3, 0.3)) * 100.0) / 100.0);
            }
            result.add(PriceIndexVO.builder().name(crop).data(data).build());
        } else {
            for (Map.Entry<String, double[]> entry : priceData.entrySet()) {
                List<Double> data = new ArrayList<>();
                for (double v : entry.getValue()) {
                    data.add(Math.round((v + r.nextDouble(-0.3, 0.3)) * 100.0) / 100.0);
                }
                result.add(PriceIndexVO.builder().name(entry.getKey()).data(data).build());
            }
        }
        return result;
    }

    // ==================== 接口9: 地区对比 ====================
    public RegionCompareVO getRegionCompare(String regionA, String regionB, String timeRange) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        List<String> dimensions = List.of("病害数量", "诊断次数", "种植面积", "产量指数", "AI覆盖率", "预警响应");
        List<Integer> scoresA = new ArrayList<>();
        List<Integer> scoresB = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            scoresA.add(r.nextInt(50, 100));
            scoresB.add(r.nextInt(50, 100));
        }
        return RegionCompareVO.builder()
                .dimensions(dimensions)
                .regionA(scoresA)
                .regionB(scoresB)
                .build();
    }
}
