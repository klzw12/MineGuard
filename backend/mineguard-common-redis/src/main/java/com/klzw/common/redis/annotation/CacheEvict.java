package com.klzw.common.redis.annotation;

import java.lang.annotation.*;

/**
 * 缓存清除注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheEvict {

    /**
     * 缓存键前缀
     */
    String keyPrefix();

    /**
     * 缓存键后缀（支持SpEL表达式）
     */
    String keySuffix() default "";

    /**
     * 是否清除所有匹配的缓存
     */
    boolean allEntries() default false;
}
