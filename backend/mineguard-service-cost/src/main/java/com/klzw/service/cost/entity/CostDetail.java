package com.klzw.service.cost.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
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

    private String description;

    private String paymentMethod;

    private LocalDateTime costDate;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}