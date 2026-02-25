package com.klzw.common.core.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";


    public static String formatDate(LocalDate date) {
        return date == null ? null : date.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
    }

    public static LocalDate parseDate(String dateStr) {
        return StringUtils.isBlank(dateStr) ? null : LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return StringUtils.isBlank(dateTimeStr) ? null : LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(DATETIME_FORMAT));
    }

    public static LocalDate nowDate() {
        return LocalDate.now();
    }

    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now();
    }
}
