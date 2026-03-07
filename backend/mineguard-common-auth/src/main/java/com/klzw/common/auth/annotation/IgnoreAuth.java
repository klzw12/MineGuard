package com.klzw.common.auth.annotation;

import java.lang.annotation.*;

/**
 * 忽略认证注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreAuth {
}
