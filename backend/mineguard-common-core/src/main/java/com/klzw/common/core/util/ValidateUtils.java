package com.klzw.common.core.util;

public class ValidateUtils {

    public static boolean isPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return false;
        }
        return phone.matches("^1[3-9]\\d{9}$");
    }

    public static boolean isEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean isIdCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return false;
        }
        return idCard.matches("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");
    }

    public static boolean isNumber(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return str.matches("^-?\\d+(\\.\\d+)?$");
    }

    public static boolean isInteger(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return str.matches("^-?\\d+$");
    }

    public static boolean isPositiveNumber(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return str.matches("^\\d+(\\.\\d+)?$");
    }

    public static boolean isPositiveInteger(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return str.matches("^[1-9]\\d*$");
    }
}
