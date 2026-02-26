package com.klzw.common.core.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具类
 * 提供常用的日期时间格式化和解析功能
 */
public class DateUtils {
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT);

    /**
     * 格式化日期
     * @param date 日期
     * @return 格式化后的日期字符串，如果date为null则返回null
     */
    public static String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMATTER);
    }

    /**
     * 格式化日期时间
     * @param dateTime 日期时间
     * @return 格式化后的日期时间字符串，如果dateTime为null则返回null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 格式化时间
     * @param time 时间
     * @return 格式化后的时间字符串，如果time为null则返回null
     */
    public static String formatTime(LocalTime time) {
        return time == null ? null : time.format(TIME_FORMATTER);
    }

    /**
     * 解析日期字符串
     * @param dateStr 日期字符串
     * @return 解析后的日期，如果字符串为空则返回null
     */
    public static LocalDate parseDate(String dateStr) {
        return StringUtils.isBlank(dateStr) ? null : LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * 解析日期时间字符串
     * @param dateTimeStr 日期时间字符串
     * @return 解析后的日期时间，如果字符串为空则返回null
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return StringUtils.isBlank(dateTimeStr) ? null : LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    /**
     * 解析时间字符串
     * @param timeStr 时间字符串
     * @return 解析后的时间，如果字符串为空则返回null
     */
    public static LocalTime parseTime(String timeStr) {
        return StringUtils.isBlank(timeStr) ? null : LocalTime.parse(timeStr, TIME_FORMATTER);
    }

    /**
     * 获取当前日期
     * @return 当前日期
     */
    public static LocalDate nowDate() {
        return LocalDate.now();
    }

    /**
     * 获取当前日期时间
     * @return 当前日期时间
     */
    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前时间
     * @return 当前时间
     */
    public static LocalTime nowTime() {
        return LocalTime.now();
    }
}
