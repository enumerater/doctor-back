package com.enumerate.disease_detection.Utils;

import java.util.Random;

/**
 * 验证码生成工具类
 */
public class CaptchaUtils {

    /**
     * 生成6位数字验证码
     */
    public static String generate6DigitCode() {
        Random random = new Random();
        // 生成100000~999999之间的随机数
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}