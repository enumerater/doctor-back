package com.enumerate.disease_detection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // 开启异步支持
public class DiseaseDetectionApplication {

    public static void main(String[] args) {
        // 缩短JDK HttpClient连接池的空闲超时时间（秒），避免复用被服务端关闭的连接导致Connection reset
        System.setProperty("jdk.httpclient.keepalive.timeout", "30");
        SpringApplication.run(DiseaseDetectionApplication.class, args);
    }

}
