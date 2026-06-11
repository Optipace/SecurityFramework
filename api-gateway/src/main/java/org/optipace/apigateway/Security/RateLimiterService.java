package org.optipace.apigateway.Security;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private final Cache<String, Bucket> buckets =
            Caffeine.newBuilder()
                    .expireAfterAccess(1, TimeUnit.HOURS)
                    .maximumSize(1000)
                    .build();


    private Bucket createBucket() {


        Bandwidth limit = Bandwidth.builder()
                .capacity(100)
                .refillGreedy(100, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public Bucket resolveBucket(String userId) {
        return buckets.get(userId, key -> createBucket());
    }
}