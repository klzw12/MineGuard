package com.klzw.service.ai.controller;

import com.klzw.common.core.client.PythonClient;
import com.klzw.common.core.result.Result;
import com.klzw.common.redis.annotation.RateLimit;
import com.klzw.service.ai.dto.ProviderSwitchDTO;
import com.klzw.service.ai.service.AiService;
import com.klzw.service.ai.vo.AnalysisResultVO;
import com.klzw.service.ai.vo.ProviderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final PythonClient pythonClient;

    @RateLimit(keyPrefix = "ai_analyze", limit = 10, window = 1, message = "AI分析请求过于频繁，请稍后再试")
    @PostMapping("/analyze/statistics")
    public Result<AnalysisResultVO> analyzeStatisticsData(@RequestBody Map<String, Object> statisticsData) {
        log.debug("分析统计数据：{}", statisticsData);
        Map<String, Object> result = aiService.analyzeStatisticsData(statisticsData);
        AnalysisResultVO vo = AnalysisResultVO.builder()
                .status("success")
                .content(result)
                .build();
        return Result.success(vo);
    }

    @RateLimit(keyPrefix = "ai_analyze", limit = 10, window = 1, message = "AI分析请求过于频繁，请稍后再试")
    @PostMapping("/analyze/cost")
    public Result<AnalysisResultVO> analyzeCostData(@RequestBody Map<String, Object> costData) {
        log.debug("分析成本数据：{}", costData);
        Map<String, Object> result = aiService.analyzeCostData(costData);
        AnalysisResultVO vo = AnalysisResultVO.builder()
                .status("success")
                .content(result)
                .build();
        return Result.success(vo);
    }

    @RateLimit(keyPrefix = "ai_report", limit = 5, window = 1, message = "AI报表生成请求过于频繁，请稍后再试")
    @PostMapping("/generate/financial-report")
    public Result<AnalysisResultVO> generateFinancialReport(@RequestBody Map<String, Object> financialData) {
        log.debug("生成财务报表：{}", financialData);
        Map<String, Object> result = aiService.generateFinancialReport(financialData);
        AnalysisResultVO vo = AnalysisResultVO.builder()
                .status("success")
                .content(result)
                .build();
        return Result.success(vo);
    }

    @RateLimit(keyPrefix = "ai_suggest", limit = 10, window = 1, message = "AI建议生成请求过于频繁，请稍后再试")
    @PostMapping("/generate/optimization-suggestions")
    public Result<AnalysisResultVO> generateOptimizationSuggestions(@RequestBody Map<String, Object> analysisData) {
        log.debug("生成优化建议：{}", analysisData);
        Map<String, Object> result = aiService.generateOptimizationSuggestions(analysisData);
        AnalysisResultVO vo = AnalysisResultVO.builder()
                .status("success")
                .content(result)
                .build();
        return Result.success(vo);
    }

    @RateLimit(keyPrefix = "ai_dispatch", limit = 10, window = 1, message = "AI调度建议请求过于频繁，请稍后再试")
    @PostMapping("/generate/dispatch-suggestions")
    public Result<AnalysisResultVO> generateDispatchSuggestions(@RequestBody Map<String, Object> dispatchData) {
        log.debug("为调度服务提供智能建议：{}", dispatchData);
        Map<String, Object> result = aiService.generateDispatchSuggestions(dispatchData);
        AnalysisResultVO vo = AnalysisResultVO.builder()
                .status("success")
                .content(result)
                .build();
        return Result.success(vo);
    }

    @GetMapping("/provider/current")
    public Result<ProviderVO> getCurrentProvider() {
        String currentProvider = aiService.getCurrentProvider();
        ProviderVO vo = ProviderVO.builder()
                .currentProvider(currentProvider)
                .status("success")
                .build();
        return Result.success(vo);
    }

    @PostMapping("/provider/switch")
    public Result<ProviderVO> switchProvider(@RequestBody ProviderSwitchDTO request) {
        String provider = request.getProvider();
        log.debug("切换AI提供商至：{}", provider);
        boolean success = aiService.switchProvider(provider);
        ProviderVO vo = ProviderVO.builder()
                .currentProvider(success ? provider : aiService.getCurrentProvider())
                .status(success ? "success" : "failed")
                .build();
        return success ? Result.success("切换成功", vo) : Result.fail("切换失败，不支持的AI提供商");
    }

    @RateLimit(keyPrefix = "ai_behavior", limit = 10, window = 1, message = "AI驾驶行为分析请求过于频繁，请稍后再试")
    @PostMapping("/analyze/driving-behavior")
    public Result<AnalysisResultVO> analyzeDrivingBehavior(@RequestBody Map<String, Object> trackData) {
        log.debug("分析驾驶行为：{}", trackData);
        Map<String, Object> result = aiService.analyzeDrivingBehavior(trackData);
        AnalysisResultVO vo = AnalysisResultVO.builder()
                .status((String) result.get("status"))
                .message((String) result.get("message"))
                .analysis((Map<String, Object>) result.get("analysis"))
                .cleaningReport((Map<String, Object>) result.get("cleaning_report"))
                .build();
        return Result.success(vo);
    }

    @RateLimit(keyPrefix = "ai_clean", limit = 20, window = 1, message = "数据清洗请求过于频繁，请稍后再试")
    @PostMapping("/clean/driving-data")
    public Result<Map<String, Object>> cleanDrivingData(@RequestBody Map<String, Object> drivingData) {
        log.debug("清洗驾驶数据：{}", drivingData);
        Map<String, Object> result = pythonClient.cleanDrivingData(drivingData);
        return Result.success(result);
    }

    @RateLimit(keyPrefix = "ai_clean", limit = 20, window = 1, message = "数据清洗请求过于频繁，请稍后再试")
    @PostMapping("/clean/statistics-data")
    public Result<Map<String, Object>> cleanStatisticsData(@RequestBody Map<String, Object> statisticsData) {
        log.debug("清洗统计数据：{}", statisticsData);
        Map<String, Object> result = pythonClient.cleanStatisticsData(statisticsData);
        return Result.success(result);
    }

    @RateLimit(keyPrefix = "ai_clean", limit = 20, window = 1, message = "数据清洗请求过于频繁，请稍后再试")
    @PostMapping("/clean/cost-data")
    public Result<Map<String, Object>> cleanCostData(@RequestBody Map<String, Object> costData) {
        log.debug("清洗成本数据：{}", costData);
        Map<String, Object> result = pythonClient.cleanCostData(costData);
        return Result.success(result);
    }
}
