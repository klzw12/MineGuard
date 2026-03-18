package com.klzw.common.core.domain.dto;

import lombok.Data;

/**
 * 行程响应DTO
 */
@Data
public class TripResponse {
    private int code;
    private String message;
    private TripData data;

    @Data
    public static class TripData {
        private Long id;
        private Long vehicleId;
        private Long driverId;
        private String status;
    }
}