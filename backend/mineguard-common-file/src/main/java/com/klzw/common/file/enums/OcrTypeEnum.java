package com.klzw.common.file.enums;

import lombok.Getter;

/**
 * OCR类型枚举 - 合并认证类型和OCR类型
 */
@Getter
public enum OcrTypeEnum {
    IDCARD_FRONT("idcard_front", "身份证正面"),
    IDCARD_BACK("idcard_back", "身份证反面"),
    DRIVING_LICENSE("driving_license", "驾驶证"),
    EMERGENCY_CERT("emergency_cert", "紧急救援资格证"),
    REPAIR_CERT("repair_cert", "维修资格证"),
    LICENSE_PLATE("license_plate", "车牌照"),
    VEHICLE_LICENSE_FRONT("vehicle_license_front", "行驶证正面"),
    VEHICLE_LICENSE_BACK("vehicle_license_back", "行驶证反面"),
    TEXT("text", "通用文字识别");

    private final String code;
    private final String description;

    OcrTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 获取枚举值
     */
    public static OcrTypeEnum getByCode(String code) {
        for (OcrTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return TEXT;
    }
}
