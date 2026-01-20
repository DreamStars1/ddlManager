package com.ddl.manager.domain.statistics.service.impl;

import com.ddl.manager.domain.statistics.dto.ApiStatistics;
import com.ddl.manager.domain.statistics.service.StatisticsService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
        // 空值校验
        if (apiName == null || apiPath == null) {
            throw new IllegalArgumentException("API名称和路径不能为空");
        }

        // 更新内存统计
        ApiStatistics stats = inMemoryStats.computeIfAbsent(apiPath, k -> new ApiStatistics(apiName, apiPath));
        stats.getCallCount().incrementAndGet();
        stats.setLastCallTime(System.currentTimeMillis());

        try {
            // 更新Redis统计 - 使用Long类型避免类型转换问题
            String countKey = REDIS_KEY_PREFIX + "count:" + apiPath;
            redisTemplate.opsForValue().increment(countKey, 1);
            redisTemplate.expire(countKey, EXPIRATION_TIME, TimeUnit.SECONDS);

            String nameKey = REDIS_KEY_PREFIX + "name:" + apiPath;
            redisTemplate.opsForValue().set(nameKey, apiName, EXPIRATION_TIME, TimeUnit.SECONDS);

            String firstKey = REDIS_KEY_PREFIX + "first:" + apiPath;
            // 只设置一次首次调用时间
            if (Boolean.FALSE.equals(redisTemplate.hasKey(firstKey))) {
                redisTemplate.opsForValue().set(firstKey, System.currentTimeMillis(),
                        EXPIRATION_TIME, TimeUnit.SECONDS);
            }

            String lastKey = REDIS_KEY_PREFIX + "last:" + apiPath;
            redisTemplate.opsForValue().set(lastKey, System.currentTimeMillis(),
                    EXPIRATION_TIME, TimeUnit.SECONDS);
        } catch (Exception e) {
            // Redis操作失败时仅打印日志，不影响内存统计
            System.err.println("Redis统计更新失败: " + e.getMessage());
        }
    }

    @Override
    public List<ApiStatistics> getAllStatistics() {
        List<ApiStatistics> result = new ArrayList<>();

        try {
            // 从Redis获取所有统计数据
            Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "count:*");
            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    try {
                        String apiPath = key.replace(REDIS_KEY_PREFIX + "count:", "");

                        // 安全获取Redis值，增加空值判断和类型转换
                        String apiName = (String) redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + "name:" + apiPath);
                        Object countObj = redisTemplate.opsForValue().get(key);
                        Object firstCallObj = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + "first:" + apiPath);
                        Object lastCallObj = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + "last:" + apiPath);

                        // 空值校验
                        if (apiName == null || apiPath == null) {
                            continue;
                        }

                        ApiStatistics stats = new ApiStatistics(apiName, apiPath);

                        // 安全转换计数（兼容Long/Integer）
                        if (countObj != null) {
                            int count = 0;
                            if (countObj instanceof Long) {
                                count = ((Long) countObj).intValue();
                            } else if (countObj instanceof Integer) {
                                count = (Integer) countObj;
                            }
                            stats.getCallCount().set(count);
                        }

                        // 时间戳转换
                        if (firstCallObj instanceof Long) {
                            stats.setFirstCallTime((Long) firstCallObj);
                        }
                        if (lastCallObj instanceof Long) {
                            stats.setLastCallTime((Long) lastCallObj);
                        }

                        result.add(stats);
                    } catch (Exception e) {
                        // 单个Key处理失败不影响整体
                        System.err.println("处理Redis统计数据失败 key=" + key + ": " + e.getMessage());
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("获取Redis统计数据失败: " + e.getMessage());
        }

        // 如果Redis中没有数据，使用内存中的数据
        if (result.isEmpty()) {
            result.addAll(inMemoryStats.values());
        }

        return result;
    }

    @Override
    public void resetAllStatistics() {
        try {
            // 1. 重置Redis中的所有API统计数据（精准匹配前缀）
            Set<String> allStatsKeys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
            if (allStatsKeys != null && !allStatsKeys.isEmpty()) {
                redisTemplate.delete(allStatsKeys);
            }
        } catch (Exception e) {
            System.err.println("重置Redis统计数据失败: " + e.getMessage());
        }

        // 2. 重置内存中的统计数据
        inMemoryStats.clear();
    }
}