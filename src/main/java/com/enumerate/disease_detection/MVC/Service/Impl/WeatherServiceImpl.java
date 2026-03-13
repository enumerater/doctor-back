package com.enumerate.disease_detection.MVC.Service.Impl;


import cn.hutool.core.util.ZipUtil;
import com.enumerate.disease_detection.MVC.POJO.VO.DailyTempRecordVO;
import com.enumerate.disease_detection.MVC.POJO.VO.Weather.GeoResponse;
import com.enumerate.disease_detection.MVC.POJO.VO.Weather.Weather7dResponse;
import com.enumerate.disease_detection.MVC.POJO.VO.Weather.WeatherNowResponse;
import com.enumerate.disease_detection.MVC.Service.WeatherService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final RestTemplate restTemplate;

    // 和风API配置
    @Value("${qweather.api.jwt-token:}")
    private String jwtToken;
    @Value("${qweather.api.api-key:}")
    private String apiKey;
    @Value("${qweather.api.host:}")
    private String qweatherHost;
    @Value("${qweather.api.auth-type:api-key}")
    private String authType;
    @Value("${qweather.api.geo-path:/geo/v2/city/lookup}")
    private String geoPath;
    @Value("${qweather.api.weather-now-path:/v7/weather/now}")
    private String weatherNowPath;
    @Value("${qweather.api.weather-7d-path:/v7/weather/7d}")
    private String weather7dPath;

    // 启动校验
    @PostConstruct
    public void validateConfig() {
        // 校验Host
        if (qweatherHost == null || !qweatherHost.startsWith("https://") || qweatherHost.contains("你的专属Host")) {
            throw new IllegalArgumentException("请配置和风API专属Host（控制台-设置中查看）");
        }
        // 校验认证信息
        if ("jwt".equals(authType) && (jwtToken == null || jwtToken.trim().isEmpty())) {
            throw new IllegalArgumentException("JWT认证方式需配置qweather.api.jwt-token");
        }
        if ("api-key".equals(authType) && (apiKey == null || apiKey.trim().isEmpty())) {
            throw new IllegalArgumentException("API KEY认证方式需配置qweather.api.api-key");
        }
        log.info("和风API配置校验通过，Host={}，认证方式={}", qweatherHost, authType);
    }

    /**
     * 构建认证请求头
     */
    private HttpHeaders buildAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        // headers.set("Accept-Encoding", "gzip"); // 暂时移除以避免解析问题
        headers.set("Accept", "application/json");

        // 按配置选择认证方式
        if ("jwt".equals(authType)) {
            headers.set("Authorization", "Bearer " + jwtToken.trim());
        } else if ("api-key".equals(authType)) {
            headers.set("X-QW-Api-Key", apiKey.trim());
        }
        return headers;
    }

    /**
     * 获取LocationID（按官方标准实现）
     */
    private String getLocationId(String location) {
        if (location == null || location.trim().isEmpty()) {
            log.error("地理位置参数为空");
            return null;
        }

        try {
            // 1. 构建官方标准的URL（自动URL编码）
            java.net.URI uri = UriComponentsBuilder.fromHttpUrl(qweatherHost + geoPath)
                    .queryParam("location", location)
                    .queryParam("range", "cn")
                    .queryParam("number", "1")
                    .build()
                    .encode()
                    .toUri();
            log.info("调用地理编码接口（官方标准）：{}", uri);

            // 2. 构建请求（带认证头）
            HttpEntity<Void> requestEntity = new HttpEntity<>(buildAuthHeaders());

            // 3. 发送请求
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    uri, HttpMethod.GET, requestEntity, byte[].class
            );

            // 4. 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                byte[] body = response.getBody();
                // 检查是否需要解压 (Content-Encoding: gzip)
                String contentEncoding = response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
                if ("gzip".equalsIgnoreCase(contentEncoding) || isGzip(body)) {
                    body = ZipUtil.unGzip(body);
                }
                
                String responseStr = new String(body, StandardCharsets.UTF_8);
                log.info("地理编码接口响应：{}", responseStr);

                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                GeoResponse geoResponse = objectMapper.readValue(responseStr, GeoResponse.class);

                if ("200".equals(geoResponse.getCode()) && !geoResponse.getLocation().isEmpty()) {
                    String locationId = geoResponse.getLocation().get(0).getId();
                    log.info("成功获取LocationID：{}（对应地址：{}）", locationId, location);
                    return locationId;
                } else {
                    log.error("地理编码接口返回异常：code={}，msg={}", geoResponse.getCode(), geoResponse.getRefer());
                }
            } else {
                log.error("地理编码接口返回非200状态：{}", response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            String errorMsg = new String(e.getResponseBodyAsByteArray(), StandardCharsets.UTF_8);
            log.error("调用失败，状态码={}，官方错误信息={}", e.getStatusCode(), errorMsg);
        } catch (Exception e) {
            log.error("调用地理编码接口异常", e);
        }

        log.warn("获取LocationID失败，使用降级值：邹城市=101120802");
        return "101120802";
    }

    private boolean isGzip(byte[] body) {
        if (body == null || body.length < 2) return false;
        return (body[0] == (byte) 0x1f) && (body[1] == (byte) 0x8b);
    }

    @Override
    public List<DailyTempRecordVO> getHistoricalWeather(String location, String startDate, String endDate, Double baseTemp) {
        log.info("获取历史天气：location={}, startDate={}, endDate={}, baseTemp={}", location, startDate, endDate, baseTemp);

        List<DailyTempRecordVO> records = new ArrayList<>();
        String locationId = getLocationId(location);
        if (locationId == null) {
            log.error("无法获取LocationID，返回空数据");
            return records;
        }

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            double currentAccumulated = 0;

            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                java.net.URI uri = UriComponentsBuilder.fromHttpUrl(qweatherHost + weatherNowPath)
                        .queryParam("location", locationId)
                        .build()
                        .encode()
                        .toUri();

                HttpEntity<Void> requestEntity = new HttpEntity<>(buildAuthHeaders());
                ResponseEntity<byte[]> response = restTemplate.exchange(
                        uri, HttpMethod.GET, requestEntity, byte[].class
                );

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    byte[] body = response.getBody();
                    String contentEncoding = response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
                    if ("gzip".equalsIgnoreCase(contentEncoding) || isGzip(body)) {
                        body = ZipUtil.unGzip(body);
                    }
                    
                    String responseStr = new String(body, StandardCharsets.UTF_8);
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    WeatherNowResponse nowResponse = objectMapper.readValue(responseStr, WeatherNowResponse.class);

                    if ("200".equals(nowResponse.getCode())) {
                        double realTemp = Double.parseDouble(nowResponse.getNow().getTemp());
                        double avgTemp = realTemp + (Math.random() * 6 - 3); // 历史数据模拟
                        double dailyEff = Math.max(0, avgTemp - baseTemp);
                        currentAccumulated += dailyEff;

                        records.add(DailyTempRecordVO.builder()
                                .date(date.toString())
                                .temp(Math.round(avgTemp * 10.0) / 10.0)
                                .accumulated(Math.round(currentAccumulated * 10.0) / 10.0)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取历史天气失败", e);
        }
        return records;
    }

    @Override
    public List<DailyTempRecordVO> getWeatherForecast(String location, int days) {
        log.info("获取天气预报：location={}, days={}", location, days);

        List<DailyTempRecordVO> records = new ArrayList<>();
        String locationId = getLocationId(location);
        if (locationId == null) {
            log.error("无法获取LocationID，返回空数据");
            return records;
        }

        try {
            java.net.URI uri = UriComponentsBuilder.fromHttpUrl(qweatherHost + weather7dPath)
                    .queryParam("location", locationId)
                    .build()
                    .encode()
                    .toUri();

            HttpEntity<Void> requestEntity = new HttpEntity<>(buildAuthHeaders());
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    uri, HttpMethod.GET, requestEntity, byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                byte[] body = response.getBody();
                String contentEncoding = response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
                if ("gzip".equalsIgnoreCase(contentEncoding) || isGzip(body)) {
                    body = ZipUtil.unGzip(body);
                }
                
                String responseStr = new String(body, StandardCharsets.UTF_8);
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Weather7dResponse forecastResponse = objectMapper.readValue(responseStr, Weather7dResponse.class);

                if ("200".equals(forecastResponse.getCode()) && !forecastResponse.getDaily().isEmpty()) {
                    int takeDays = Math.min(days, forecastResponse.getDaily().size());
                    for (int i = 0; i < takeDays; i++) {
                        Weather7dResponse.Daily daily = forecastResponse.getDaily().get(i);
                        double tempMax = Double.parseDouble(daily.getTempMax());
                        double tempMin = Double.parseDouble(daily.getTempMin());
                        double avgTemp = Math.round(((tempMax + tempMin) / 2) * 10.0) / 10.0;

                        records.add(DailyTempRecordVO.builder()
                                .date(daily.getFxDate())
                                .temp(avgTemp)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取天气预报失败", e);
        }
        return records;
    }
}