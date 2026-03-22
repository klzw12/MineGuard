package com.klzw.service.trip.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 行程轨迹MongoDB文档
 * 用于存储历史轨迹数据
 */
@Data
@Document(collection = "trip_tracks")
public class TripTrackDocument {

    @Id
    private String id;

    private Long tripId;

    private Long vehicleId;

    private Double longitude;

    private Double latitude;

    private Double speed;

    private Double direction;

    private Double altitude;

    private Double mileage;

    private Integer fuelLevel;

    private LocalDateTime recordTime;

    private LocalDateTime createTime;

    private Integer status;

    private String location;

}
