package com.chrizlove.vortexpay.common_lib.ratelimit;

public interface RateLimiter {

    RateLimitResult check(String key,int maxRequestsAllowed, long windowSeconds);
}
