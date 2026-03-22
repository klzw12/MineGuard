package com.klzw.service.ai.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.ai.dto.ProviderSwitchDTO;
import com.klzw.service.ai.service.AiService;
import com.klzw.service.ai.service.PythonServiceClient;
import com.klzw.service.ai.vo.AnalysisResultVO;
import com.klzw.service.ai.vo.ProviderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final PythonServiceClient pythonServiceClient;

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

    @PostMapping("/export/statistics")
    public ResponseEntity<byte[]> exportStatistics(@RequestBody Map<String, Object> exportRequest) {
        log.debug("导出统计报表：{}", exportRequest);
        byte[] data = pythonServiceClient.exportStatistics(exportRequest);
        String filename = generateFilename("statistics", exportRequest);
        return buildExcelResponse(data, filename);
    }

    @PostMapping("/export/trip-report")
    public ResponseEntity<byte[]> exportTripReport(@RequestBody Map<String, Object> exportRequest) {
        log.debug("导出行程报表：{}", exportRequest);
        byte[] data = pythonServiceClient.exportTripReport(exportRequest);
        String filename = generateFilename("trip_report", exportRequest);
        return buildExcelResponse(data, filename);
    }

    @PostMapping("/export/cost-report")
    public ResponseEntity<byte[]> exportCostReport(@RequestBody Map<String, Object> exportRequest) {
        log.debug("导出成本报表：{}", exportRequest);
        byte[] data = pythonServiceClient.exportCostReport(exportRequest);
        String filename = generateFilename("cost_report", exportRequest);
        return buildExcelResponse(data, filename);
    }

    @PostMapping("/export/vehicle-report")
    public ResponseEntity<byte[]> exportVehicleReport(@RequestBody Map<String, Object> exportRequest) {
        log.debug("导出车辆报表：{}", exportRequest);
        byte[] data = pythonServiceClient.exportVehicleReport(exportRequest);
        String filename = generateFilename("vehicle_report", exportRequest);
        return buildExcelResponse(data, filename);
    }

    @PostMapping("/export/driver-report")
    public ResponseEntity<byte[]> exportDriverReport(@RequestBody Map<String, Object> exportRequest) {
        log.debug("导出司机报表：{}", exportRequest);
        byte[] data = pythonServiceClient.exportDriverReport(exportRequest);
        String filename = generateFilename("driver_report", exportRequest);
        return buildExcelResponse(data, filename);
    }

    @PostMapping("/clean/driving-data")
    public Result<Map<String, Object>> cleanDrivingData(@RequestBody Map<String, Object> drivingData) {
        log.debug("清洗驾驶数据：{}", drivingData);
        Map<String, Object> result = pythonServiceClient.cleanDrivingData(drivingData);
        return Result.success(result);
    }

    @PostMapping("/clean/statistics-data")
    public Result<Map<String, Object>> cleanStatisticsData(@RequestBody Map<String, Object> statisticsData) {
        log.debug("清洗统计数据：{}", statisticsData);
        Map<String, Object> result = pythonServiceClient.cleanStatisticsData(statisticsData);
        return Result.success(result);
    }

    @PostMapping("/clean/cost-data")
    public Result<Map<String, Object>> cleanCostData(@RequestBody Map<String, Object> costData) {
        log.debug("清洗成本数据：{}", costData);
        Map<String, Object> result = pythonServiceClient.cleanCostData(costData);
        return Result.success(result);
    }

    private String generateFilename(String prefix, Map<String, Object> request) {
        String customName = (String) request.get("filename");
        if (customName != null && !customName.isEmpty()) {
            return customName + ".xlsx";
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return prefix + "_" + timestamp + ".xlsx";
    }

    private ResponseEntity<byte[]> buildExcelResponse(byte[] data, String filename) {
        if (data == null || data.length == 0) {
            return ResponseEntity.noContent().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        headers.setContentDispositionFormData("attachment", encodedFilename);
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
}
