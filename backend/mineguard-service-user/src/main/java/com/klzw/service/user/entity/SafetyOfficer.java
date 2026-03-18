package com.klzw.service.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("safety_officer")
public class SafetyOfficer extends BaseEntity {

    private Long userId;

    private String officerName;

    private Integer gender;

    private String idCard;

    private String idCardFrontUrl;

    private String idCardBackUrl;

    private String emergencyCertUrl;

    private String certNumber;

    private String trainingProject;

    private String validPeriod;

    private Integer status;
}
