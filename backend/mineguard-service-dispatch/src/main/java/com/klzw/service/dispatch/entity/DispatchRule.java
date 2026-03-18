package com.klzw.service.dispatch.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dispatch_rule")
public class DispatchRule extends BaseEntity {

    private String ruleName;

    private String ruleCode;

    private Integer ruleType;

    private String ruleContent;

    private Integer status;

    private String description;
}