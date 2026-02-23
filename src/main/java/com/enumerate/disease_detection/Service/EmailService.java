package com.enumerate.disease_detection.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 邮件发送服务
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private RedisService redisService;

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送验证码邮件
     * @param toEmail 接收邮箱
     * @param code 验证码
     */
    public void sendCaptchaEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail); // 发件人
        message.setTo(toEmail);     // 收件人
        message.setSubject("【验证码】登录/注册验证"); // 邮件标题
        // 邮件内容
        message.setText(String.format("您好！您的登录/注册验证码是：%s，有效期5分钟，请及时使用。", code));

        // 发送邮件
        javaMailSender.send(message);

        // 存redis
        redisService.setWithExpire(toEmail, code, 5 * 60, TimeUnit.SECONDS);
    }
}