package com.enumerate.disease_detection.Controller;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.POJO.VO.dashboard.*;
import com.enumerate.disease_detection.Service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/data/dashboard")
@Slf4j
@CrossOrigin
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /** 接口1: 地区树 */
    @GetMapping("/regions")
    public Result<List<RegionVO>> getRegions() {
        return Result.success(dashboardService.getRegions());
    }

    /** 接口2: KPI 汇总数据 */
    @GetMapping("/region-summary")
    public Result<RegionSummaryVO> getRegionSummary(
            @RequestParam String regionCode,
            @RequestParam String timeRange) {
        return Result.success(dashboardService.getRegionSummary(regionCode, timeRange));
    }

    /** 接口3: 地图标记点 */
    @GetMapping("/map-markers")
    public Result<List<MapMarkerVO>> getMapMarkers(
            @RequestParam String regionCode,
            @RequestParam(required = false) String types,
            @RequestParam(required = false) String crops) {
        return Result.success(dashboardService.getMapMarkers(regionCode, types, crops));
    }

    /** 接口4: 病害预警列表 */
    @GetMapping("/disease-alerts")
    public Result<List<DiseaseAlertVO>> getDiseaseAlerts(
            @RequestParam String regionCode,
            @RequestParam(required = false) Integer limit) {
        return Result.success(dashboardService.getDiseaseAlerts(regionCode, limit));
    }

    /** 接口5: 诊断趋势 */
    @GetMapping("/diagnosis-trend")
    public Result<List<DiagnosisTrendVO>> getDiagnosisTrend(
            @RequestParam String regionCode,
            @RequestParam String timeRange,
            @RequestParam(required = false) String crops) {
        return Result.success(dashboardService.getDiagnosisTrend(regionCode, timeRange, crops));
    }

    /** 接口6: 作物种类分布 */
    @GetMapping("/crop-distribution")
    public Result<List<CropDistributionVO>> getCropDistribution(
            @RequestParam String regionCode) {
        return Result.success(dashboardService.getCropDistribution(regionCode));
    }

    /** 接口7: 天气数据 */
    @GetMapping("/weather")
    public Result<WeatherVO> getWeather(
            @RequestParam String regionCode,
            @RequestParam(required = false) String timeRange) {
        log.info("regionCode: {}, timeRange: {}", regionCode, timeRange);
        return Result.success(dashboardService.getWeather(regionCode, timeRange));
    }

    /** 接口8: 农产品价格趋势 */
    @GetMapping("/price-index")
    public Result<List<PriceIndexVO>> getPriceIndex(
            @RequestParam(required = false) String crop,
            @RequestParam String timeRange) {
        return Result.success(dashboardService.getPriceIndex(crop, timeRange));
    }

    /** 接口9: 地区对比 */
    @GetMapping("/region-compare")
    public Result<RegionCompareVO> getRegionCompare(
            @RequestParam String regionA,
            @RequestParam String regionB,
            @RequestParam String timeRange) {
        return Result.success(dashboardService.getRegionCompare(regionA, regionB, timeRange));
    }
}
