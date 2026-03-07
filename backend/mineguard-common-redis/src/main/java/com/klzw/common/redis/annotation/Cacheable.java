package com.klzw.common.redis.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cacheable {

    /**
     * 缓存键前缀
     */
    String keyPrefix();

    /**
     * 缓存键后缀（支持SpEL表达式）
     */
    String keySuffix() default "";

    /**
     * 过期时间
     */
    long expire() default 3600;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 是否使用同步锁
     */
    boolean sync() default false;
}
