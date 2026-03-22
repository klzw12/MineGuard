package com.klzw.common.core.client;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Python 服务客户端
 */
@HttpExchange
public interface PythonClient {
    
    /**
     * 分析驾驶行为
     * 
     * @param tripId 行程 ID
     * @return Python 评分（0-100）
     */
    @org.springframework.web.bind.annotation.GetMapping("/api/analysis/driving-behavior")
    Integer analyzeDrivingBehavior(@RequestParam("tripId") Long tripId);
}
