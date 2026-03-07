package com.klzw.common.core.util;

import java.util.Objects;

/**
 * 字符串工具类
 * 提供常用的字符串操作方法
 */
public class StringUtils {
    /**
     * 判断字符串是否为空白
     * @param str 字符串
     * @return 如果字符串为null或trim后为空，则返回true，否则返回false
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空白
     * @param str 字符串
     * @return 如果字符串不为null且trim后不为空，则返回true，否则返回false
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 判断字符串是否为空
     * @param str 字符串
     * @return 如果字符串为null或为空，则返回true，否则返回false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否不为空
     * @param str 字符串
     * @return 如果字符串不为null且不为空，则返回true，否则返回false
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 去除字符串首尾空白
     * @param str 字符串
     * @return 去除首尾空白后的字符串，如果str为null则返回null
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 如果字符串为空白，则返回默认值
     * @param str 字符串
     * @param defaultValue 默认值
     * @return 如果字符串为空白，则返回默认值，否则返回原字符串
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    /**
     * 如果字符串为空，则返回默认值
     * @param str 字符串
     * @param defaultValue 默认值
     * @return 如果字符串为空，则返回默认值，否则返回原字符串
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    /**
     * 比较两个字符串是否相等
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 如果两个字符串相等，则返回true，否则返回false
     */
    public static boolean equals(String str1, String str2) {
        return Objects.equals(str1, str2);
    }

    /**
     * 忽略大小写比较两个字符串是否相等
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 如果两个字符串忽略大小写后相等，则返回true，否则返回false
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
    }

    /**
     * 连接数组元素为字符串
     * @param array 数组
     * @param separator 分隔符
     * @return 连接后的字符串，如果array为null则返回null
     */
    public static String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(array[i]);
        }
        return sb.toString();
    }
}
