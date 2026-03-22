package com.klzw.service.warning.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("warning_rule")
public class WarningRule extends BaseEntity {

    private String ruleName;

    private String ruleCode;

    private Integer warningType;

    private Integer warningLevel;

    private String thresholdValue;

    private String pushRoles;

    private String ruleConfig;

    private Integer status;

    private String description;
}
