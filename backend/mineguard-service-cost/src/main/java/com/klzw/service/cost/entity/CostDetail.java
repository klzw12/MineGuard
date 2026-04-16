package com.klzw.service.cost.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("cost_detail")
public class CostDetail {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String costNo;

    private Integer costType;

    private String costName;

    private BigDecimal amount;

    private Long vehicleId;

    private Long tripId;

    private Long userId;

    private LocalDate costDate;

    private String paymentMethod;

    private String invoiceNo;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;

    @TableLogic
    private Integer deleted;

    private String remark;
}
