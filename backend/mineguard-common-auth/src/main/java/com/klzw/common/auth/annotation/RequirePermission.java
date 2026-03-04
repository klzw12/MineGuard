package com.klzw.common.auth.annotation;

import java.lang.annotation.*;

/**
 * 权限注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 权限标识
     */
    String[] value();

    /**
     * 是否需要所有权限
     */
    boolean requireAll() default true;
}
