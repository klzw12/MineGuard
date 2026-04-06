package com.klzw.common.core.client;

import com.klzw.common.core.result.Result;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Map;

/**
 * 运输服务客户端（用于调用 statistics-service 的 transport 统计接口）
 */
@HttpExchange
public interface TransportClient {

    /**
     * 查询运输统计
     */
    @GetExchange("/api/statistics/transport")
    Result<Map<String, Object>> getTransportStatistics(
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate
    );

    /**
     * 查询车辆统计
     */
    @GetExchange("/api/statistics/vehicle")
    Result<java.util.List<Map<String, Object>>> getVehicleStatistics(
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate
    );
}
