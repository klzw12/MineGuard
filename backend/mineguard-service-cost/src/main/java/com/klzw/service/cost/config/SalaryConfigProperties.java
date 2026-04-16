package com.klzw.service.cost.config;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 薪酬配置属性类
 * 用于读取和管理薪酬相关的配置项
 */

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mineguard.cost.salary")
public class SalaryConfigProperties {

    // 最低保障工资
    private BigDecimal minSalary= BigDecimal.valueOf(2500);

    // 每月工作日天数
    private Integer workDaysPerMonth = 22;

    // 请假阈值（天）
    private Integer leaveThreshold = 2;

    // 旷工阈值（天）
    private Integer absenteeismThreshold = 15;

    // 旷工超过阈值是否禁用用户
    private Boolean absenteeismDisableUser = false;

    // 全勤奖
    private BigDecimal fullAttendanceBonus = BigDecimal.valueOf(500);

    // 加班倍率
    private BigDecimal overtimeRate = BigDecimal.valueOf(1.5);

    // 迟到早退扣除率
    private BigDecimal lateEarlyLeaveDeductionRate = BigDecimal.valueOf(1.0);

    // 请假扣除率
    private BigDecimal leaveDeductionRate = BigDecimal.valueOf(1.0);

    // 薪酬计算缓存过期时间（小时）
    private Integer salaryCalculateCacheExpire=24;

    // 薪酬计算定时器配置
    private String salaryCalculateCron = "0 0 2 1 * ?";
}
