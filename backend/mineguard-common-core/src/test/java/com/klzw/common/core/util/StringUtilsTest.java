package com.klzw.common.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * 字符串工具类测试
 */
public class StringUtilsTest {

    @Test
    @DisplayName("测试字符串是否为空")
    public void testIsBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank("   "));
        assertFalse(StringUtils.isBlank("  测试  "));
        assertFalse(StringUtils.isBlank("测试"));
    }

    @Test
    @DisplayName("测试字符串是否不为空")
    public void testIsNotBlank() {
        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank("   "));
        assertTrue(StringUtils.isNotBlank("  测试  "));
        assertTrue(StringUtils.isNotBlank("测试"));
    }

    @Test
    @DisplayName("测试字符串是否为空（不包含空白字符）")
    public void testIsEmpty() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty("   "));
        assertFalse(StringUtils.isEmpty("测试"));
    }

    @Test
    @DisplayName("测试字符串是否不为空（不包含空白字符）")
    public void testIsNotEmpty() {
        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty("   "));
        assertTrue(StringUtils.isNotEmpty("测试"));
    }

    @Test
    @DisplayName("测试字符串 trim 方法")
    public void testTrim() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("", StringUtils.trim("   "));
        assertEquals("测试", StringUtils.trim("  测试  "));
        assertEquals("测试", StringUtils.trim("测试"));
    }

    @Test
    @DisplayName("测试字符串为空时返回默认值")
    public void testDefaultIfBlank() {
        assertEquals("默认值", StringUtils.defaultIfBlank(null, "默认值"));
        assertEquals("默认值", StringUtils.defaultIfBlank("", "默认值"));
        assertEquals("默认值", StringUtils.defaultIfBlank("   ", "默认值"));
        assertEquals("测试", StringUtils.defaultIfBlank("测试", "默认值"));
    }

    @Test
    @DisplayName("测试字符串为空时返回默认值（不包含空白字符）")
    public void testDefaultIfEmpty() {
        assertEquals("默认值", StringUtils.defaultIfEmpty(null, "默认值"));
        assertEquals("默认值", StringUtils.defaultIfEmpty("", "默认值"));
        assertEquals("   ", StringUtils.defaultIfEmpty("   ", "默认值"));
        assertEquals("测试", StringUtils.defaultIfEmpty("测试", "默认值"));
    }

    @Test
    @DisplayName("测试字符串相等比较")
    public void testEquals() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, ""));
        assertFalse(StringUtils.equals("", null));
        assertTrue(StringUtils.equals("", ""));
        assertTrue(StringUtils.equals("测试", "测试"));
        assertFalse(StringUtils.equals("测试1", "测试2"));
    }

    @Test
    @DisplayName("测试字符串忽略大小写比较")
    public void testEqualsIgnoreCase() {
        assertTrue(StringUtils.equalsIgnoreCase(null, null));
        assertFalse(StringUtils.equalsIgnoreCase(null, ""));
        assertFalse(StringUtils.equalsIgnoreCase("", null));
        assertTrue(StringUtils.equalsIgnoreCase("", ""));
        assertTrue(StringUtils.equalsIgnoreCase("测试", "测试"));
        assertTrue(StringUtils.equalsIgnoreCase("TEST", "test"));
        assertFalse(StringUtils.equalsIgnoreCase("测试1", "测试2"));
    }

    @Test
    @DisplayName("测试数组连接成字符串")
    public void testJoin() {
        assertNull(StringUtils.join(null, ","));
        assertEquals("a,b,c", StringUtils.join(new Object[]{"a", "b", "c"}, ","));
        assertEquals("a b c", StringUtils.join(new Object[]{"a", "b", "c"}, " "));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}, null));
    }
}
