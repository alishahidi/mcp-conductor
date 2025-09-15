package net.alishahidi.mcpconductor.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int capacity;
    private final int refillTokens;
    private final Duration refillDuration;

    public RateLimiter(@Value("${rate-limit.capacity:100}") int capacity,
                       @Value("${rate-limit.refill-tokens:100}") int refillTokens,
                       @Value("${rate-limit.refill-duration-minutes:1}") int refillMinutes) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillDuration = Duration.ofMinutes(refillMinutes);
    }

    public boolean tryConsume(String key) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket());
        boolean consumed = bucket.tryConsume(1);

        if (!consumed) {
            log.warn("Rate limit exceeded for key: {}", key);
        }

        return consumed;
    }

    public boolean tryConsume(String key, int tokens) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket());
        boolean consumed = bucket.tryConsume(tokens);

        if (!consumed) {
            log.warn("Rate limit exceeded for key: {} (requested {} tokens)", key, tokens);
        }

        return consumed;
    }

    public long getAvailableTokens(String key) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket());
        return bucket.getAvailableTokens();
    }

    public void reset(String key) {
        buckets.remove(key);
        log.info("Rate limit reset for key: {}", key);
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(capacity,
                Refill.intervally(refillTokens, refillDuration));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}