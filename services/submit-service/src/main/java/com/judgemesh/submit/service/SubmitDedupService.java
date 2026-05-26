package com.judgemesh.submit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
public class SubmitDedupService {

    private static final Duration WINDOW = Duration.ofSeconds(1);

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final ConcurrentMap<String, Instant> localCache = new ConcurrentHashMap<>();

    public boolean tryAcquire(long userId, long problemId) {
        String key = buildKey(userId, problemId);
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            Boolean inserted = redisTemplate.opsForValue().setIfAbsent(key, "1", WINDOW);
            return Boolean.TRUE.equals(inserted);
        }

        Instant now = Instant.now();
        Instant previous = localCache.putIfAbsent(key, now);
        if (previous == null) {
            return true;
        }
        if (Duration.between(previous, now).compareTo(WINDOW) >= 0) {
            localCache.put(key, now);
            return true;
        }
        return false;
    }

    private String buildKey(long userId, long problemId) {
        return "dedup:submit:%s:%s".formatted(userId, problemId);
    }
}
