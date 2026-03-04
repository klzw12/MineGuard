package com.klzw.common.auth.annotation;

import java.lang.annotation.*;

/**
 * 角色注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 角色标识
     */
    String[] value();

    /**
     * 是否需要所有角色
     */
    boolean requireAll() default true;
}
