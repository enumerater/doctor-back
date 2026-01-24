package com.enumerate.disease_detection.Properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret; // 密钥
    private Long expiration; // 过期时间(毫秒)
}
