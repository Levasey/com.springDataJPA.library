package com.springdatajpa.library.support;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Фиксированное окно в одну минуту на ключ клиента (in-memory).
 */
public final class MinuteBucketRateLimiter {

    private final ConcurrentHashMap<String, long[]> buckets = new ConcurrentHashMap<>();

    /**
     * @return {@code true}, если запрос разрешён; {@code false}, если лимит исчерпан
     */
    public boolean tryAcquire(String key, int maxPerMinute) {
        if (maxPerMinute <= 0) {
            return true;
        }
        long nowMinute = Instant.now().getEpochSecond() / 60;
        long[] slot = buckets.computeIfAbsent(key, k -> new long[] {nowMinute, 0});
        synchronized (slot) {
            if (slot[0] != nowMinute) {
                slot[0] = nowMinute;
                slot[1] = 0;
            }
            if (slot[1] >= maxPerMinute) {
                return false;
            }
            slot[1]++;
            return true;
        }
    }
}
