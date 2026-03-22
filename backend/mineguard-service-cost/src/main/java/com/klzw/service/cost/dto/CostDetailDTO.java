package com.klzw.service.cost.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CostDetailDTO {

    private Long id;

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

    private String remark;
}
