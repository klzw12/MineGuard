package com.klzw.service.cost.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CostDetailVO {

    private Long id;

    private String costNo;

    private Integer costType;

    private String costTypeName;

    private String costName;

    private BigDecimal amount;

    private Long vehicleId;

    private String vehicleNo;

    private Long tripId;

    private String tripNo;

    private Long userId;

    private String userName;

    private LocalDate costDate;

    private String paymentMethod;

    private String invoiceNo;

    private String description;

    private LocalDateTime createTime;
}
