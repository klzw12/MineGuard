package com.klzw.common.database.annotation;

import java.lang.annotation.*;

/**
 * 数据源切换注解
 * <p>
 * 主要功能：
 * 1. 标记方法或类使用指定的数据源
 * 2. 支持方法级别的数据源切换
 * 3. 配合DataSourceAspect实现自动切换
 * <p>
 * 使用示例：
 * <pre>
 * &#64;DataSource("slave")
 * public List<User> queryUsers() {
 *     return userMapper.selectList(null);
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {

    /**
     * 数据源名称
     * <p>
     * 可选值：
     * - master: 主数据源（默认）
     * - slave: 从数据源
     * <p>
     * 默认值：master
     *
     * @return 数据源名称
     */
    String value() default "master";
}
