package com.klzw.service.dispatch.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("route_template")
public class RouteTemplate extends BaseEntity {

    private String routeName;

    private String startLocation;

    private String endLocation;

    private Double startLongitude;

    private Double startLatitude;

    private Double endLongitude;

    private Double endLatitude;

    private BigDecimal distance;

    private Integer estimatedDuration;

    private Integer status;
}
