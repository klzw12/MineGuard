package com.klzw.common.core.util;

/**
 * 验证工具类
 * 提供常用的数据验证方法
 */
public class ValidateUtils {

    /**
     * 验证手机号
     * @param phone 手机号
     * @return 如果是有效的手机号则返回true，否则返回false
     */
    public static boolean isPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return false;
        }
        return phone.matches("^1[3-9]\\d{9}$");
    }

    /**
     * 验证邮箱
     * @param email 邮箱
     * @return 如果是有效的邮箱则返回true，否则返回false
     */
    public static boolean isEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * 验证身份证号
     * @param idCard 身份证号
     * @return 如果是有效的身份证号则返回true，否则返回false
     */
    public static boolean isIdCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return false;
        }
        return idCard.matches("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");
    }

    /**
     * 验证是否为数字（包括整数和小数）
     * @param str 字符串
     * @return 如果是有效的数字则返回true，否则返回false
     */
    public static boolean isNumber(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return str.matches("^-?\\d+(\\.\\d+)?$");
    }

    /**
     * 验证是否为整数
     * @param str 字符串
     * @return 如果是有效的整数则返回true，否则返回false
     */
    public static boolean isInteger(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return str.matches("^-?\\d+$");
    }

    /**
     * 验证是否为正数（包括正整数和正小数）
     * @param str 字符串
     * @return 如果是有效的正数则返回true，否则返回false
     */
    public static boolean isPositiveNumber(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return str.matches("^\\d+(\\.\\d+)?$");
    }

    /**
     * 验证是否为正整数
     * @param str 字符串
     * @return 如果是有效的正整数则返回true，否则返回false
     */
    public static boolean isPositiveInteger(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return str.matches("^[1-9]\\d*$");
    }
}
