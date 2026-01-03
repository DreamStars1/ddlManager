// src/main/java/com/ddl/manager/domain/statistics/service/impl/StatisticsServiceImpl.java
package com.ddl.manager.domain.statistics.service.impl;

import com.ddl.manager.domain.statistics.dto.ApiStatistics;
import com.ddl.manager.domain.statistics.service.StatisticsService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    // 内存存储 - 用于基础计数
    private final Map<String, ApiStatistics> inMemoryStats = new ConcurrentHashMap<>();

    // Redis存储 - 用于持久化和分布式环境
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY_PREFIX = "api_stats:";
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60; // 7天过期

    @Override
    public void incrementCallCount(String apiName, String apiPath) {
        String key = apiPath;

        // 更新内存统计
        ApiStatistics stats = inMemoryStats.computeIfAbsent(key, k -> new ApiStatistics(apiName, apiPath));
        stats.getCallCount().incrementAndGet();
        stats.setLastCallTime(System.currentTimeMillis());

        // 更新Redis统计
        redisTemplate.opsForValue().increment(REDIS_KEY_PREFIX + "count:" + key, 1);
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + "name:" + key, apiName, EXPIRATION_TIME, TimeUnit.SECONDS);

        // 只设置一次首次调用时间
        if (!redisTemplate.hasKey(REDIS_KEY_PREFIX + "first:" + key)) {
            redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + "first:" + key, System.currentTimeMillis(),
                    EXPIRATION_TIME, TimeUnit.SECONDS);
        }
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + "last:" + key, System.currentTimeMillis(),
                EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    @Override
    public List<ApiStatistics> getAllStatistics() {
        List<ApiStatistics> result = new ArrayList<>();

        // 从Redis获取所有统计数据
        Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "count:*");
        if (keys != null) {
            for (String key : keys) {
                String apiPath = key.replace(REDIS_KEY_PREFIX + "count:", "");
                String apiName = (String) redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + "name:" + apiPath);
                Integer count = (Integer)redisTemplate.opsForValue().get(key);
                Long firstCall = (Long) redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + "first:" + apiPath);
                Long lastCall = (Long) redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + "last:" + apiPath);

                ApiStatistics stats = new ApiStatistics(apiName, apiPath);
                stats.getCallCount().set(count.intValue());
                stats.setFirstCallTime(firstCall);
                stats.setLastCallTime(lastCall);

                result.add(stats);
            }
        }

        // 如果Redis中没有数据，使用内存中的数据
        if (result.isEmpty()) {
            result.addAll(inMemoryStats.values());
        }

        return result;
    }

    @Override
    public void resetAllStatistics() {
        // 1. 重置Redis中的所有API统计数据（精准匹配前缀）
        // 匹配 "api_stats:" 开头的所有Key
        Set<String> allStatsKeys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
        if (allStatsKeys != null && !allStatsKeys.isEmpty()) {
            // 批量删除所有匹配的Key（高效批量操作）
            redisTemplate.delete(allStatsKeys);
        }

        // 2. 重置内存中的统计数据
        inMemoryStats.clear();
    }
}