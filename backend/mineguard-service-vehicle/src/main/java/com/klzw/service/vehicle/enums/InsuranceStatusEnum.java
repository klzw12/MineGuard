package com.klzw.service.vehicle.enums;

/**
 * 车辆保险状态枚举
 */
public enum InsuranceStatusEnum {
    
    NOT_INSURED(0, "未投保"),
    INSURED(1, "已投保/有效"),
    EXPIRED(2, "已过期"),
    CLAIMING(3, "理赔中");
    
    private final Integer value;
    private final String label;
    
    InsuranceStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
    
    public Integer getValue() {
        return value;
    }
    
    public String getLabel() {
        return label;
    }
    
    /**
     * 根据值获取枚举
     */
    public static InsuranceStatusEnum valueOf(Integer value) {
        if (value == null) {
            return null;
        }
        for (InsuranceStatusEnum status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
