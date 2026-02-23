package com.enumerate.disease_detection.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    // 注入 StringRedisTemplate（推荐优先使用，避免序列化问题）
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // ========== 字符串（String）操作 ==========
    // 设置缓存（无过期时间）
    public void set(String key, String value) {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set(key, value);
    }

    // 设置缓存（带过期时间）
    public void setWithExpire(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    // 获取缓存
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    // 删除缓存
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    // 判断key是否存在
    public boolean exists(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    // ========== 哈希（Hash）操作 ==========
    public void hSet(String hashKey, String field, String value) {
        stringRedisTemplate.opsForHash().put(hashKey, field, value);
    }

    public String hGet(String hashKey, String field) {
        return (String) stringRedisTemplate.opsForHash().get(hashKey, field);
    }

    // ========== 列表（List）操作 ==========
    public void lPush(String key, String value) {
        stringRedisTemplate.opsForList().leftPush(key, value);
    }

    public String lPop(String key) {
        return stringRedisTemplate.opsForList().leftPop(key);
    }

    // ========== 集合（Set）操作 ==========
    public void sAdd(String key, String... values) {
        stringRedisTemplate.opsForSet().add(key, values);
    }

    public boolean sIsMember(String key, String value) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(key, value));
    }
}