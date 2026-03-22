package com.klzw.service.cost.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CostQueryDTO {

    private Long vehicleId;

    private Long userId;

    private Integer costType;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
