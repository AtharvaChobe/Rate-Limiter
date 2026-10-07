package com.example.rate_limiter.controller;
import com.example.rate_limiter.dto.RateLimitRequest;
import com.example.rate_limiter.service.RedisRateLimiter;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class RateLimitController {

    private final RedisRateLimiter redisRateLimiter;

    public RateLimitController(RedisRateLimiter redisRateLimiter) {
        this.redisRateLimiter = redisRateLimiter;
    }

    @PostMapping("/check")
    public String check(@RequestBody RateLimitRequest request) {

        boolean allowed = redisRateLimiter.isAllowed(
                request.getApiKey(),
                request.getLimit(),
                request.getWindowSeconds()
        );

        return allowed
                ? "Request allowed"
                : "Rate limit exceeded";
    }
}
