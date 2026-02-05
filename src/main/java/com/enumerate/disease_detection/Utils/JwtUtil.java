package com.enumerate.disease_detection.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JwtUtil {
    /**
     * 生成jwt
     * 使用Hs256算法, 私匙使用固定秘钥
     *
     * @param secretKey jwt秘钥
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    设置的信息
     * @return
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 指定签名的时候使用的签名算法，也就是header那部分
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        // 生成JWT的时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // 设置jwt的body
        JwtBuilder builder = Jwts.builder()
                // 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
                .setClaims(claims)
                // 设置签名使用的签名算法和签名使用的秘钥
                .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8))
                // 设置过期时间
                .setExpiration(exp);

        return builder.compact();
    }

    /**
     * Token解密
     *
     * @param secretKey jwt秘钥 此秘钥一定要保留好在服务端, 不能暴露出去, 否则sign就可以被伪造, 如果对接多个客户端建议改造成多个
     * @param token     加密后的token
     * @return
     */
    public static Claims parseJWT(String secretKey, String token) {
        // ========== 核心验证日志：打印工具类实际接收到的token ==========
        log.info("JwtUtil接收到的token：【{}】，token是否为null：【{}】", token, token == null);
        // ========== 原有防御性校验 ==========
        if (token == null) {
            throw new IllegalArgumentException("JWT Token不能为null");
        }
        String cleanToken = token.trim();
        if (cleanToken.isEmpty()) {
            throw new IllegalArgumentException("JWT Token不能为空串或纯空白字符");
        }
        // 兼容Bearer前缀
        if (cleanToken.startsWith("Bearer ")) {
            cleanToken = cleanToken.substring(7).trim();
            log.info("JwtUtil处理Bearer前缀，截取后token：【{}】", cleanToken);
            if (cleanToken.isEmpty()) {
                throw new IllegalArgumentException("JWT Token仅包含Bearer前缀，无实际令牌内容");
            }
        }
        // 解析逻辑
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(cleanToken)
                .getBody();
        log.info("JwtUtil解析Token成功，获取到Claims：【{}】", claims);
        return claims;
    }

}
