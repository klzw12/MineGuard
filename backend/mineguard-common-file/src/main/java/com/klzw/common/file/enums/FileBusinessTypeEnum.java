package com.klzw.common.file.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileBusinessTypeEnum {
    USER_AVATAR(1, "avatar", "用户头像"),
    ID_CARD(2, "id-card", "身份证"),
    DRIVING_LICENSE(3, "driving-license", "驾驶证"),
    VEHICLE_LICENSE(4, "vehicle-license", "行驶证"),
    VEHICLE_PHOTO(5, "vehicle-photo", "车辆照片"),
    CHAT_IMAGE(6, "chat-image", "聊天图片"),
    OTHER(99, "other", "其他");

    private final int code;
    private final String folder;
    private final String description;

    public static FileBusinessTypeEnum fromCode(int code) {
        for (FileBusinessTypeEnum type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return OTHER;
    }
}
