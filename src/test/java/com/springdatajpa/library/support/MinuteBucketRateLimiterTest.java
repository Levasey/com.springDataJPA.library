package com.springdatajpa.library.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinuteBucketRateLimiterTest {

    @Test
    void tryAcquire_allowsUpToLimit_thenBlocksUntilNextWindow() {
        MinuteBucketRateLimiter limiter = new MinuteBucketRateLimiter();
        String key = "ip|scope";

        assertTrue(limiter.tryAcquire(key, 2));
        assertTrue(limiter.tryAcquire(key, 2));
        assertFalse(limiter.tryAcquire(key, 2));
    }

    @Test
    void tryAcquire_maxZero_alwaysAllows() {
        MinuteBucketRateLimiter limiter = new MinuteBucketRateLimiter();
        assertTrue(limiter.tryAcquire("k", 0));
    }
}
