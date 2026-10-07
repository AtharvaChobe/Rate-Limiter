package com.example.rate_limiter.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RedisRateLimiterTest {

    @Autowired
    private RedisRateLimiter redisRateLimiter;

    @Test
    void shouldAllowOnlyCapacityRequestsConcurrently()
            throws Exception {

        String apiKey = "test-" + System.currentTimeMillis();

        int capacity = 10;
        int totalRequests = 50;

        ExecutorService executor =
                Executors.newFixedThreadPool(10);

        List<Callable<Boolean>> tasks = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {

            tasks.add(() ->
                    redisRateLimiter.isAllowed(
                            apiKey,
                            capacity,
                            60
                    )
            );
        }

        List<Future<Boolean>> results =
                executor.invokeAll(tasks);

        int allowed = 0;

        for (Future<Boolean> result : results) {

            if (result.get()) {
                allowed++;
            }
        }

        executor.shutdown();

        assertEquals(capacity, allowed);
    }
}