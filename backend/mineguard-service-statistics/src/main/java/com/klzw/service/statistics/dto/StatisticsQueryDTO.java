package com.klzw.service.statistics.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StatisticsQueryDTO {

    private LocalDate startDate;

    private LocalDate endDate;

    private Long vehicleId;

    private Long userId;

    private String period;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
