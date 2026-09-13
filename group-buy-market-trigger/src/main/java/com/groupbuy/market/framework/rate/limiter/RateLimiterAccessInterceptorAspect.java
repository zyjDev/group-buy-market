package com.groupbuy.market.framework.rate.limiter;

import com.google.common.util.concurrent.RateLimiter;
import com.groupbuy.market.framework.rate.limiter.types.annotations.RateLimiterAccessInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A process-local access limiter backed by Guava's token bucket.
 */
@Slf4j
@Aspect
@Component
public class RateLimiterAccessInterceptorAspect {

    private static final long BLOCK_MILLIS = 60_000L;

    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    private final Map<String, Long> blockedUntil = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimiter)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimiterAccessInterceptor rateLimiter) throws Throwable {
        String accessKey = resolveAccessKey(joinPoint, rateLimiter.key());
        Long blockedTime = blockedUntil.get(accessKey);
        long now = System.currentTimeMillis();
        if (blockedTime != null && blockedTime > now) {
            return invokeFallback(joinPoint, rateLimiter.fallbackMethod());
        }
        if (blockedTime != null) {
            blockedUntil.remove(accessKey);
        }

        RateLimiter limiter = rateLimiters.computeIfAbsent(accessKey,
                key -> RateLimiter.create(Math.max(rateLimiter.permitsPerSecond(), 0.1D)));
        if (limiter.tryAcquire()) {
            return joinPoint.proceed();
        }

        if (rateLimiter.blacklistCount() <= 1) {
            blockedUntil.put(accessKey, now + BLOCK_MILLIS);
        }
        log.warn("访问限流触发 key:{}", accessKey);
        return invokeFallback(joinPoint, rateLimiter.fallbackMethod());
    }

    private String resolveAccessKey(ProceedingJoinPoint joinPoint, String keyName) {
        if (keyName != null && !keyName.isEmpty()) {
            for (Object argument : joinPoint.getArgs()) {
                Object value = readProperty(argument, keyName);
                if (value != null) {
                    return keyName + ':' + value;
                }
            }
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringTypeName() + '#' + signature.getName();
    }

    private Object readProperty(Object target, String property) {
        if (target == null) {
            return null;
        }
        String suffix = Character.toUpperCase(property.charAt(0)) + property.substring(1);
        for (String methodName : new String[]{"get" + suffix, "is" + suffix}) {
            try {
                Method method = target.getClass().getMethod(methodName);
                return method.invoke(target);
            } catch (Exception ignored) {
                // Try the next standard accessor shape.
            }
        }
        return null;
    }

    private Object invokeFallback(ProceedingJoinPoint joinPoint, String fallbackMethod) throws Throwable {
        if (fallbackMethod == null || fallbackMethod.isEmpty()) {
            throw new IllegalStateException("Rate limited but no fallback method was configured");
        }

        Object target = joinPoint.getTarget();
        Class<?>[] parameterTypes = ((MethodSignature) joinPoint.getSignature()).getParameterTypes();
        try {
            Method fallback = target.getClass().getMethod(fallbackMethod, parameterTypes);
            return fallback.invoke(target, joinPoint.getArgs());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Rate limiter fallback method not found: " + fallbackMethod, e);
        }
    }

}
