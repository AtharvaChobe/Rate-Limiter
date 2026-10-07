package com.example.rate_limiter.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedisRateLimiter {
    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<Long> rateLimitScript;

    public RedisRateLimiter(
            RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = new DefaultRedisScript<>();
        this.rateLimitScript.setLocation(
                new ClassPathResource("rate-limit.lua")
        );
        this.rateLimitScript.setResultType(Long.class);
    }

    public boolean isAllowed(
            String apiKey,
            int limit,
            int windowSeconds) {
        double refillRate =
                (double) limit / windowSeconds;
        long currentTime =
                System.currentTimeMillis();
        String key = "rate-limit:" + apiKey;
        Long result = redisTemplate.execute(
                rateLimitScript,
                List.of(key),
                String.valueOf(limit),
                String.valueOf(refillRate),
                String.valueOf(currentTime)
        );
        return result != null && result == 1;
    }
}
