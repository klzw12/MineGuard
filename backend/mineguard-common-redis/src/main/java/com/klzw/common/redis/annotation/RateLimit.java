package com.klzw.common.redis.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流键前缀
     */
    String keyPrefix() default "rate_limit";

    /**
     * 限流键后缀（支持SpEL表达式）
     */
    String keySuffix() default "";

    /**
     * 限制次数
     */
    int limit() default 10;

    /**
     * 时间窗口
     */
    long window() default 60;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 限流提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
}
