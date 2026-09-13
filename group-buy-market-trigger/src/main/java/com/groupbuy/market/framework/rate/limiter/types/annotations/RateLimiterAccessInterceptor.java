package com.groupbuy.market.framework.rate.limiter.types.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applies an access rate limit to a controller method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiterAccessInterceptor {

    String key() default "";

    String fallbackMethod() default "";

    double permitsPerSecond() default 1.0D;

    int blacklistCount() default 1;

}
