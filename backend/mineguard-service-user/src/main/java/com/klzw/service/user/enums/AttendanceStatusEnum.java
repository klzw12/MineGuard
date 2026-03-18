package com.klzw.service.user.enums;

import lombok.Getter;

/**
 * 出勤状态枚举
 * 对应db.sql中driver_attendance表的status字段
 */
@Getter
public enum AttendanceStatusEnum {
    NORMAL(1, "正常"),
    LATE(2, "迟到"),
    EARLY_LEAVE(3, "早退"),
    ABSENT(4, "缺勤");

    private final int value;
    private final String label;

    AttendanceStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static AttendanceStatusEnum getByValue(int value) {
        for (AttendanceStatusEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
