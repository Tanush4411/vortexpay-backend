package com.chrizlove.vortexpay.common_lib.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name="app.rate-limit.method",havingValue = "fixed")
public class FixedWindowRateLimiter implements RateLimiter {

    private final StringRedisTemplate redis;

    @Override
    public RateLimitResult check(String key, int maxRequestsAllowed, long windowSeconds) {

        String redisKey="ratelimit:fixed:"+key;

        Long count=redis.opsForValue().increment(redisKey);

        if(count==null) return RateLimitResult.allowed(maxRequestsAllowed); //redis unavailable, so just allow requests

        if(count==1){
            redis.expire(redisKey, Duration.ofSeconds(windowSeconds));
        }

        if(count>maxRequestsAllowed){
            Long ttl=redis.getExpire(redisKey, TimeUnit.SECONDS);
            int retryAfter=(ttl!=null && ttl>0) ? ttl.intValue() : (int) windowSeconds;
            return RateLimitResult.denied(retryAfter);
        }
        return RateLimitResult.allowed((int)(maxRequestsAllowed-count));
    }
}
