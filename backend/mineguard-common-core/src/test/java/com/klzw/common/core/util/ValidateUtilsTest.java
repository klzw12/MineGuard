package com.klzw.common.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * 验证工具类测试
 */
public class ValidateUtilsTest {

    @Test
    @DisplayName("测试手机号验证")
    public void testIsPhone() {
        assertTrue(ValidateUtils.isPhone("13812345678"));
        assertTrue(ValidateUtils.isPhone("19912345678"));
        assertFalse(ValidateUtils.isPhone(null));
        assertFalse(ValidateUtils.isPhone(""));
        assertFalse(ValidateUtils.isPhone("   "));
        assertFalse(ValidateUtils.isPhone("12345678901")); // 开头不是13-9
        assertFalse(ValidateUtils.isPhone("1381234567")); // 长度不足
        assertFalse(ValidateUtils.isPhone("138123456789")); // 长度过长
    }

    @Test
    @DisplayName("测试邮箱验证")
    public void testIsEmail() {
        assertTrue(ValidateUtils.isEmail("test@example.com"));
        assertTrue(ValidateUtils.isEmail("test.user@example.co.uk"));
        assertFalse(ValidateUtils.isEmail(null));
        assertFalse(ValidateUtils.isEmail(""));
        assertFalse(ValidateUtils.isEmail("   "));
        assertFalse(ValidateUtils.isEmail("test@")); // 缺少域名
        assertFalse(ValidateUtils.isEmail("@example.com")); // 缺少用户名
        assertFalse(ValidateUtils.isEmail("test.example.com")); // 缺少@
    }

    @Test
    @DisplayName("测试身份证号验证")
    public void testIsIdCard() {
        assertTrue(ValidateUtils.isIdCard("110101199001011234"));
        assertTrue(ValidateUtils.isIdCard("11010119900101123X"));
        assertFalse(ValidateUtils.isIdCard(null));
        assertFalse(ValidateUtils.isIdCard(""));
        assertFalse(ValidateUtils.isIdCard("   "));
        assertFalse(ValidateUtils.isIdCard("11010119900101123")); // 长度不足
        assertFalse(ValidateUtils.isIdCard("1101011990010112345")); // 长度过长
        assertFalse(ValidateUtils.isIdCard("110101199013011234")); // 月份无效
        assertFalse(ValidateUtils.isIdCard("110101199001321234")); // 日期无效
    }

    @Test
    @DisplayName("测试数字验证")
    public void testIsNumber() {
        assertTrue(ValidateUtils.isNumber("123"));
        assertTrue(ValidateUtils.isNumber("-123"));
        assertTrue(ValidateUtils.isNumber("123.45"));
        assertTrue(ValidateUtils.isNumber("-123.45"));
        assertFalse(ValidateUtils.isNumber(null));
        assertFalse(ValidateUtils.isNumber(""));
        assertFalse(ValidateUtils.isNumber("   "));
        assertFalse(ValidateUtils.isNumber("abc"));
        assertFalse(ValidateUtils.isNumber("123abc"));
    }

    @Test
    @DisplayName("测试整数验证")
    public void testIsInteger() {
        assertTrue(ValidateUtils.isInteger("123"));
        assertTrue(ValidateUtils.isInteger("-123"));
        assertFalse(ValidateUtils.isInteger(null));
        assertFalse(ValidateUtils.isInteger(""));
        assertFalse(ValidateUtils.isInteger("   "));
        assertFalse(ValidateUtils.isInteger("123.45")); // 小数
        assertFalse(ValidateUtils.isInteger("abc"));
    }

    @Test
    @DisplayName("测试正数验证")
    public void testIsPositiveNumber() {
        assertTrue(ValidateUtils.isPositiveNumber("123"));
        assertTrue(ValidateUtils.isPositiveNumber("123.45"));
        assertFalse(ValidateUtils.isPositiveNumber(null));
        assertFalse(ValidateUtils.isPositiveNumber(""));
        assertFalse(ValidateUtils.isPositiveNumber("   "));
        assertFalse(ValidateUtils.isPositiveNumber("-123")); // 负数
        assertFalse(ValidateUtils.isPositiveNumber("abc"));
    }

    @Test
    @DisplayName("测试正整数验证")
    public void testIsPositiveInteger() {
        assertTrue(ValidateUtils.isPositiveInteger("123"));
        assertFalse(ValidateUtils.isPositiveInteger(null));
        assertFalse(ValidateUtils.isPositiveInteger(""));
        assertFalse(ValidateUtils.isPositiveInteger("   "));
        assertFalse(ValidateUtils.isPositiveInteger("-123")); // 负数
        assertFalse(ValidateUtils.isPositiveInteger("123.45")); // 小数
        assertFalse(ValidateUtils.isPositiveInteger("abc"));
        assertFalse(ValidateUtils.isPositiveInteger("0")); // 零
    }
}