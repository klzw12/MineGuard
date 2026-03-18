package com.klzw.service.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("repairman")
public class Repairman extends BaseEntity {

    private Long userId;

    private String repairmanName;

    private Integer gender;

    private String idCard;

    private String idCardFrontUrl;

    private String idCardBackUrl;

    private String repairCertUrl;

    private String certNumber;

    private String repairLevel;

    private String repairType;

    private LocalDate validUntil;

    private LocalDate issueDate;

    private Integer status;
}
