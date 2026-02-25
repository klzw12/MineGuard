package com.klzw.common.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日期工具类测试
 */
public class DateUtilsTest {

    @Test
    @DisplayName("测试日期格式化")
    public void testFormatDate() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        String formattedDate = DateUtils.formatDate(date);
        assertEquals("2023-01-01", formattedDate);
        
        // 测试 null 值
        assertNull(DateUtils.formatDate(null));
    }

    @Test
    @DisplayName("测试日期时间格式化")
    public void testFormatDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
        String formattedDateTime = DateUtils.formatDateTime(dateTime);
        assertEquals("2023-01-01 12:00:00", formattedDateTime);
        
        // 测试 null 值
        assertNull(DateUtils.formatDateTime(null));
    }

    @Test
    @DisplayName("测试日期解析")
    public void testParseDate() {
        String dateStr = "2023-01-01";
        LocalDate date = DateUtils.parseDate(dateStr);
        assertEquals(LocalDate.of(2023, 1, 1), date);
        
        // 测试 null 值
        assertNull(DateUtils.parseDate(null));
        assertNull(DateUtils.parseDate(""));
        assertNull(DateUtils.parseDate("   "));
    }

    @Test
    @DisplayName("测试日期时间解析")
    public void testParseDateTime() {
        String dateTimeStr = "2023-01-01 12:00:00";
        LocalDateTime dateTime = DateUtils.parseDateTime(dateTimeStr);
        assertEquals(LocalDateTime.of(2023, 1, 1, 12, 0, 0), dateTime);
        
        // 测试 null 值
        assertNull(DateUtils.parseDateTime(null));
        assertNull(DateUtils.parseDateTime(""));
        assertNull(DateUtils.parseDateTime("   "));
    }

    @Test
    @DisplayName("测试获取当前日期")
    public void testNowDate() {
        LocalDate currentDate = DateUtils.nowDate();
        assertNotNull(currentDate);
        assertEquals(LocalDate.now(), currentDate);
    }

    @Test
    @DisplayName("测试获取当前日期时间")
    public void testNowDateTime() {
        LocalDateTime currentDateTime = DateUtils.nowDateTime();
        assertNotNull(currentDateTime);
        assertEquals(LocalDateTime.now().getDayOfYear(), currentDateTime.getDayOfYear());
    }
}