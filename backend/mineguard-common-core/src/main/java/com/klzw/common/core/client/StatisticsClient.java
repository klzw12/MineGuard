package com.klzw.common.core.client;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import java.util.Map;

/**
 * 统计服务客户端（用于调用其他服务的统计接口）
 */
@HttpExchange
public interface StatisticsClient {

    /**
     * 查询行程统计（调用 trip-service）
     */
    @GetExchange("/api/trip/statistics")
    Map<String, Object> getTripStatistics(
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate
    );

    /**
     * 查询故障统计（调用 vehicle-service）
     */
    @GetExchange("/api/vehicle/fault/statistics")
    Map<String, Object> getFaultStatistics(
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate
    );
    
    /**
     * 查询成本统计（调用 cost-service）
     */
    @GetExchange("/api/cost/statistics")
    Map<String, Object> getCostStatistics(
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate
    );
}
