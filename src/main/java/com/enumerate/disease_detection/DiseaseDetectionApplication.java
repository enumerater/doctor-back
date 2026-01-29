package com.enumerate.disease_detection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // 开启异步支持
public class DiseaseDetectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiseaseDetectionApplication.class, args);
    }

}
