package com.klzw.common.core.client;

import com.klzw.common.core.domain.dto.CostStatisticsResponseDTO;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * 成本服务客户端（用于调用 cost-service 的统计接口）
 */
@HttpExchange
public interface CostClient {

    /**
     * 查询成本统计（调用 cost-service）
     */
    @GetExchange("/api/cost/statistics")
    CostStatisticsResponseDTO getCostStatistics(
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate
    );
}
