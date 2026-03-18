package com.klzw.service.ai.controller;

import com.klzw.service.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * 分析统计数据
     * @param statisticsData 统计数据
     * @return 分析结果
     */
    @PostMapping("/analyze/statistics")
    public Map<String, Object> analyzeStatisticsData(@RequestBody Map<String, Object> statisticsData) {
        try {
            log.debug("分析统计数据：{}", statisticsData);
            return aiService.analyzeStatisticsData(statisticsData);
        } catch (Exception e) {
            log.error("分析统计数据异常", e);
            throw e;
        }
    }

    /**
     * 分析成本数据
     * @param costData 成本数据
     * @return 分析结果
     */
    @PostMapping("/analyze/cost")
    public Map<String, Object> analyzeCostData(@RequestBody Map<String, Object> costData) {
        try {
            log.debug("分析成本数据：{}", costData);
            return aiService.analyzeCostData(costData);
        } catch (Exception e) {
            log.error("分析成本数据异常", e);
            throw e;
        }
    }

    /**
     * 生成财务报表
     * @param financialData 财务数据
     * @return 报表数据
     */
    @PostMapping("/generate/financial-report")
    public Map<String, Object> generateFinancialReport(@RequestBody Map<String, Object> financialData) {
        try {
            log.debug("生成财务报表：{}", financialData);
            return aiService.generateFinancialReport(financialData);
        } catch (Exception e) {
            log.error("生成财务报表异常", e);
            throw e;
        }
    }

    /**
     * 生成优化建议
     * @param analysisData 分析数据
     * @return 优化建议
     */
    @PostMapping("/generate/optimization-suggestions")
    public Map<String, Object> generateOptimizationSuggestions(@RequestBody Map<String, Object> analysisData) {
        try {
            log.debug("生成优化建议：{}", analysisData);
            return aiService.generateOptimizationSuggestions(analysisData);
        } catch (Exception e) {
            log.error("生成优化建议异常", e);
            throw e;
        }
    }

    /**
     * 为调度服务提供智能建议
     * @param dispatchData 调度数据
     * @return 调度建议
     */
    @PostMapping("/generate/dispatch-suggestions")
    public Map<String, Object> generateDispatchSuggestions(@RequestBody Map<String, Object> dispatchData) {
        try {
            log.debug("为调度服务提供智能建议：{}", dispatchData);
            return aiService.generateDispatchSuggestions(dispatchData);
        } catch (Exception e) {
            log.error("为调度服务提供智能建议异常", e);
            throw e;
        }
    }

    /**
     * 获取当前使用的AI提供商
     * @return 当前AI提供商
     */
    @GetMapping("/provider/current")
    public Map<String, Object> getCurrentProvider() {
        try {
            String currentProvider = aiService.getCurrentProvider();
            Map<String, Object> result = new HashMap<>();
            result.put("currentProvider", currentProvider);
            result.put("status", "success");
            return result;
        } catch (Exception e) {
            log.error("获取当前AI提供商异常", e);
            throw e;
        }
    }

    /**
     * 切换AI提供商
     * @param request 包含provider字段的请求体
     * @return 切换结果
     */
    @PostMapping("/provider/switch")
    public Map<String, Object> switchProvider(@RequestBody Map<String, String> request) {
        try {
            String provider = request.get("provider");
            log.debug("切换AI提供商至：{}", provider);
            boolean success = aiService.switchProvider(provider);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? "切换成功" : "切换失败");
            return result;
        } catch (Exception e) {
            log.error("切换AI提供商异常", e);
            throw e;
        }
    }

    /**
     * 分析驾驶行为
     * @param trackData 轨迹数据
     * @return 分析结果
     */
    @PostMapping("/analyze/driving-behavior")
    public Map<String, Object> analyzeDrivingBehavior(@RequestBody Map<String, Object> trackData) {
        try {
            log.debug("分析驾驶行为：{}", trackData);
            return aiService.analyzeDrivingBehavior(trackData);
        } catch (Exception e) {
            log.error("分析驾驶行为异常", e);
            throw e;
        }
    }
}