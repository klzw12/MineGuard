package com.klzw.service.python.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.python.service.AsyncExportService;
import com.klzw.service.python.service.PythonService;
import com.klzw.service.python.vo.ExportTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/python")
@Tag(name = "Python服务代理", description = "Python服务代理接口")
@RequiredArgsConstructor
public class PythonController {

    private final PythonService pythonService;
    private final AsyncExportService asyncExportService;

    @PostMapping("/clean/driving-data")
    @Operation(summary = "清洗驾驶数据")
    public Result<Map<String, Object>> cleanDrivingData(@RequestBody Map<String, Object> drivingData) {
        return Result.success(pythonService.cleanDrivingData(drivingData));
    }

    @PostMapping("/clean/statistics-data")
    @Operation(summary = "清洗统计数据")
    public Result<Map<String, Object>> cleanStatisticsData(@RequestBody Map<String, Object> statisticsData) {
        return Result.success(pythonService.cleanStatisticsData(statisticsData));
    }

    @PostMapping("/clean/cost-data")
    @Operation(summary = "清洗成本数据")
    public Result<Map<String, Object>> cleanCostData(@RequestBody Map<String, Object> costData) {
        return Result.success(pythonService.cleanCostData(costData));
    }

    @PostMapping("/analysis/driving-behavior")
    @Operation(summary = "分析驾驶行为")
    public Result<Map<String, Object>> analyzeDrivingBehavior(@RequestBody Map<String, Object> trackData) {
        return Result.success(pythonService.analyzeDrivingBehavior(trackData));
    }

    @PostMapping("/analysis/driving-behavior/{tripId}")
    @Operation(summary = "通过行程ID分析驾驶行为")
    public Result<Integer> analyzeDrivingBehaviorByTripId(
            @Parameter(description = "行程ID") @PathVariable Long tripId) {
        return Result.success(pythonService.analyzeDrivingBehavior(tripId));
    }

    @PostMapping("/analysis/cost")
    @Operation(summary = "分析成本")
    public Result<Map<String, Object>> analyzeCost(@RequestBody Map<String, Object> costData) {
        return Result.success(pythonService.analyzeCost(costData));
    }

    @PostMapping("/analysis/vehicle-efficiency")
    @Operation(summary = "分析车辆效率")
    public Result<Map<String, Object>> analyzeVehicleEfficiency(@RequestBody Map<String, Object> vehicleData) {
        return Result.success(pythonService.analyzeVehicleEfficiency(vehicleData));
    }

    @PostMapping("/export/statistics")
    @Operation(summary = "导出统计报表")
    public Result<byte[]> exportStatistics(@RequestBody Map<String, Object> exportRequest) {
        return Result.success(pythonService.exportStatistics(exportRequest));
    }

    @PostMapping("/export/trip-report")
    @Operation(summary = "导出行程报表")
    public Result<byte[]> exportTripReport(@RequestBody Map<String, Object> exportRequest) {
        return Result.success(pythonService.exportTripReport(exportRequest));
    }

    @PostMapping("/export/cost-report")
    @Operation(summary = "导出成本报表")
    public Result<byte[]> exportCostReport(@RequestBody Map<String, Object> exportRequest) {
        return Result.success(pythonService.exportCostReport(exportRequest));
    }

    @PostMapping("/export/vehicle-report")
    @Operation(summary = "导出车辆报表")
    public Result<byte[]> exportVehicleReport(@RequestBody Map<String, Object> exportRequest) {
        return Result.success(pythonService.exportVehicleReport(exportRequest));
    }

    @PostMapping("/export/driver-report")
    @Operation(summary = "导出司机报表")
    public Result<byte[]> exportDriverReport(@RequestBody Map<String, Object> exportRequest) {
        return Result.success(pythonService.exportDriverReport(exportRequest));
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查")
    public Result<Map<String, Object>> healthCheck() {
        return Result.success(pythonService.healthCheck());
    }

    @PostMapping("/export/async/{exportType}")
    @Operation(summary = "异步导出报表", description = "exportType: statistics, trip, cost, vehicle, driver")
    public Result<String> exportAsync(
            @Parameter(description = "导出类型") @PathVariable String exportType,
            @RequestBody Map<String, Object> exportRequest) {
        String taskId = asyncExportService.submitExportTask(exportType, exportRequest);
        return Result.success(taskId);
    }

    @GetMapping("/export/status/{taskId}")
    @Operation(summary = "查询导出任务状态")
    public Result<ExportTaskVO> getExportStatus(
            @Parameter(description = "任务ID") @PathVariable String taskId) {
        ExportTaskVO task = asyncExportService.getTaskStatus(taskId);
        if (task == null) {
            return Result.fail("任务不存在");
        }
        return Result.success(task);
    }

    @GetMapping("/export/result/{taskId}")
    @Operation(summary = "下载导出结果")
    public ResponseEntity<byte[]> getExportResult(
            @Parameter(description = "任务ID") @PathVariable String taskId) {
        try {
            byte[] data = asyncExportService.getTaskResult(taskId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=export_" + taskId + ".xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/export/cancel/{taskId}")
    @Operation(summary = "取消导出任务")
    public Result<Void> cancelExport(
            @Parameter(description = "任务ID") @PathVariable String taskId) {
        asyncExportService.cancelTask(taskId);
        return Result.success();
    }

}