package com.klzw.common.core.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TripTrackDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long tripId;

    private Long vehicleId;

    private Long driverId;

    private Double longitude;

    private Double latitude;

    private Double speed;

    private Double direction;

    private Double altitude;

    private Long recordTime;

    private Integer pointIndex;
}
