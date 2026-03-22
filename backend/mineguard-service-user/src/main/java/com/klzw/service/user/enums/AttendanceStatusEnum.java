package com.klzw.service.user.enums;

import lombok.Getter;

@Getter
public enum AttendanceStatusEnum {
    NORMAL(1, "正常"),
    LATE(2, "迟到"),
    EARLY_LEAVE(3, "早退"),
    ABSENT(4, "缺勤"),
    LEAVE(5, "请假");

    private final int value;
    private final String label;

    AttendanceStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static AttendanceStatusEnum getByValue(int value) {
        for (AttendanceStatusEnum item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
