package com.aichat.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存
     * @param key 键
     * @param value 值
     * @param expireTime 过期时间（秒）
     */
    public void set(String key, Object value, long expireTime) {
        redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.SECONDS);
    }

    /**
     * 获取缓存
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     * @param key 键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 向列表右侧添加元素
     * @param key 键
     * @param value 值
     */
    public void rightPushToList(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 获取列表范围元素
     * @param key 键
     * @param start 开始位置
     * @param end 结束位置
     * @return 列表
     */
    public List<Object> rangeList(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 修剪列表
     * @param key 键
     * @param start 开始位置
     * @param end 结束位置
     */
    public void trimList(String key, long start, long end) {
        redisTemplate.opsForList().trim(key, start, end);
    }

    /**
     * 获取列表长度
     * @param key 键
     * @return 长度
     */
    public Long listSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 设置过期时间
     * @param key 键
     * @param expireTime 过期时间（小时）
     */
    public void expire(String key, long expireTime, TimeUnit timeUnit) {
        redisTemplate.expire(key, expireTime, timeUnit);
    }
}    