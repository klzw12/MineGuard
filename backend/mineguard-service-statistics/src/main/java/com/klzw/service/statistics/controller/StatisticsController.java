package com.klzw.service.statistics.controller;

import com.klzw.common.core.client.PythonClient;
import com.klzw.common.core.result.Result;
import com.klzw.service.statistics.dto.StatisticsQueryDTO;
import com.klzw.service.statistics.service.StatisticsService;
import com.klzw.service.statistics.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final PythonClient pythonClient;


    @GetMapping("/trip")
    public Result<List<TripStatisticsVO>> getTripStatistics(StatisticsQueryDTO queryDTO) {
        log.debug("获取行程统计数据：{}", queryDTO);
        List<TripStatisticsVO> list = statisticsService.getTripStatistics(queryDTO);
        return Result.success(list);
    }

    @GetMapping("/cost")
    public Result<List<CostStatisticsVO>> getCostStatistics(StatisticsQueryDTO queryDTO) {
        log.debug("获取成本统计数据：{}", queryDTO);
        List<CostStatisticsVO> list = statisticsService.getCostStatistics(queryDTO);
        return Result.success(list);
    }

    @GetMapping("/driver")
    public Result<List<DriverStatisticsVO>> getDriverStatistics(StatisticsQueryDTO queryDTO) {
        log.debug("获取司机统计数据：{}", queryDTO);
        List<DriverStatisticsVO> list = statisticsService.getDriverStatistics(queryDTO);
        return Result.success(list);
    }

    @GetMapping("/overall")
    public Result<OverallStatisticsVO> getOverallStatistics(StatisticsQueryDTO queryDTO) {
        log.debug("获取总体统计数据：{}", queryDTO);
        OverallStatisticsVO vo = statisticsService.getOverallStatistics(queryDTO);
        return Result.success(vo);
    }

    @PostMapping("/trip/calculate")
    public Result<TripStatisticsVO> calculateTripStatistics(@RequestParam("date") String date) {
        log.debug("计算行程统计数据：日期={}", date);
        TripStatisticsVO vo = statisticsService.calculateTripStatistics(date);
        return Result.success(vo);
    }

    @PostMapping("/cost/calculate")
    public Result<CostStatisticsVO> calculateCostStatistics(@RequestParam("date") String date) {
        log.debug("计算成本统计数据：日期={}", date);
        CostStatisticsVO vo = statisticsService.calculateCostStatistics(date);
        return Result.success(vo);
    }

    @PostMapping("/vehicle/calculate")
    public Result<VehicleStatisticsVO> calculateVehicleStatistics(
            @RequestParam("vehicleId") Long vehicleId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        log.debug("计算车辆统计数据：车辆ID={}, startDate={}, endDate={}", vehicleId, startDate, endDate);
        VehicleStatisticsVO vo = statisticsService.calculateVehicleStatistics(vehicleId, startDate, endDate);
        return Result.success(vo);
    }

    @PostMapping("/driver/calculate")
    public Result<DriverStatisticsVO> calculateDriverStatistics(
            @RequestParam("userId") Long userId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        log.debug("计算司机统计数据：用户 ID={}, startDate={}, endDate={}", userId, startDate, endDate);
        DriverStatisticsVO vo = statisticsService.calculateDriverStatistics(userId, startDate, endDate);
        return Result.success(vo);
    }
    
    @GetMapping("/fault")
    public Result<FaultStatisticsVO> getFaultStatistics(StatisticsQueryDTO queryDTO) {
        log.debug("获取故障统计数据：{}", queryDTO);
        FaultStatisticsVO vo = statisticsService.getFaultStatistics(queryDTO);
        return Result.success(vo);
    }
    
    @GetMapping("/fault/overall")
    public Result<FaultStatisticsVO> getFaultOverallStatistics(StatisticsQueryDTO queryDTO) {
        log.debug("获取故障总体统计数据：{}", queryDTO);
        FaultStatisticsVO vo = statisticsService.getFaultOverallStatistics(queryDTO);
        log.debug("返回故障总体统计数据：{}", vo);
        return Result.success(vo);
    }
    
    @GetMapping("/transport")
    public Result<List<TransportStatisticsVO>> getTransportStatistics(StatisticsQueryDTO queryDTO) {
        log.debug("获取运输统计数据：{}", queryDTO);
        List<TransportStatisticsVO> list = statisticsService.getTransportStatistics(queryDTO);
        return Result.success(list);
    }
    
    @PostMapping("/fault/calculate")
    public Result<Void> calculateFaultStatistics(
            @RequestParam("vehicleId") Long vehicleId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        log.debug("计算故障统计数据：车辆 ID={}, startDate={}, endDate={}", vehicleId, startDate, endDate);
        statisticsService.calculateFaultStatistics(vehicleId, startDate, endDate);
        return Result.success();
    }
    
    @PostMapping("/driver/calculate-all")
    public Result<Void> calculateAllDriverStatistics(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        log.debug("批量计算所有司机统计数据：startDate={}, endDate={}", startDate, endDate);
        statisticsService.calculateAllDriverStatistics(startDate, endDate);
        return Result.success();
    }
    
    @PostMapping("/fault/calculate-all")
    public Result<Void> calculateAllFaultStatistics(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        log.debug("批量计算所有故障统计数据：startDate={}, endDate={}", startDate, endDate);
        statisticsService.calculateAllFaultStatistics(startDate, endDate);
        return Result.success();
    }
    
    @PostMapping("/vehicle/calculate-all")
    public Result<Void> calculateAllVehicleStatistics(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        log.debug("批量计算所有车辆统计数据：startDate={}, endDate={}", startDate, endDate);
        statisticsService.calculateAllVehicleStatistics(startDate, endDate);
        return Result.success();
    }
    
    @PostMapping("/transport/calculate")
    public Result<TransportStatisticsVO> calculateTransportStatistics(@RequestParam("date") String date) {
        log.debug("计算运输统计数据：日期={}", date);
        TransportStatisticsVO vo = statisticsService.calculateTransportStatistics(date);
        return Result.success(vo);
    }
    
    @PostMapping("/calculate-all")
    public Result<Map<String, Object>> calculateAllStatistics(@RequestParam("date") String date) {
        log.info("手动触发全部统计计算：日期={}", date);
        Map<String, Object> result = new java.util.HashMap<>();
        
        try {
            TripStatisticsVO tripVO = statisticsService.calculateTripStatistics(date);
            result.put("trip", tripVO);
        } catch (Exception e) {
            log.error("计算行程统计失败", e);
            result.put("tripError", e.getMessage());
        }
        
        try {
            CostStatisticsVO costVO = statisticsService.calculateCostStatistics(date);
            result.put("cost", costVO);
        } catch (Exception e) {
            log.error("计算成本统计失败", e);
            result.put("costError", e.getMessage());
        }
        
        try {
            TransportStatisticsVO transportVO = statisticsService.calculateTransportStatistics(date);
            result.put("transport", transportVO);
        } catch (Exception e) {
            log.error("计算运输统计失败", e);
            result.put("transportError", e.getMessage());
        }
        
        result.put("date", date);
        result.put("calculatedAt", java.time.LocalDateTime.now().toString());
        
        log.info("全部统计计算完成：{}", result.keySet());
        return Result.success(result);
    }
    
    @GetMapping("/vehicle/trip-stats")
    public Result<List<VehicleTripStatsVO>> getVehicleTripStats(
            @RequestParam(value = "dimension", defaultValue = "day") String dimension,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        log.debug("获取车辆出车统计：dimension={}, startDate={}, endDate={}", dimension, startDate, endDate);
        List<VehicleTripStatsVO> list = statisticsService.getVehicleTripStats(dimension, startDate, endDate);
        return Result.success(list);
    }
    
    @GetMapping("/driver/stats-aggregated")
    public Result<List<DriverStatisticsVO>> getDriverStatsAggregated(
            @RequestParam(value = "dimension", defaultValue = "day") String dimension,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        log.debug("获取司机统计聚合：dimension={}, startDate={}, endDate={}", dimension, startDate, endDate);
        List<DriverStatisticsVO> list = statisticsService.getDriverStatsAggregated(dimension, startDate, endDate);
        return Result.success(list);
    }
    
    @PostMapping("/backfill")
    public Result<Map<String, Object>> backfillStatistics(@RequestParam("date") String date) {
        log.info("补录统计数据：日期={}", date);
        Map<String, Object> result = new java.util.HashMap<>();
        
        try {
            statisticsService.backfillStatistics(date);
            result.put("success", true);
            result.put("date", date);
            result.put("message", "补录统计完成");
        } catch (Exception e) {
            log.error("补录统计数据失败", e);
            result.put("success", false);
            result.put("date", date);
            result.put("error", e.getMessage());
        }
        
        return Result.success(result);
    }
    
    @GetMapping("/export")
    public void exportStatistics(
            @RequestParam(value = "format", defaultValue = "excel") String format,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "type", defaultValue = "overview") String type,
            HttpServletResponse response) {
        log.info("导出统计报表：format={}, startDate={}, endDate={}, type={}", format, startDate, endDate, type);
        
        try {
            Map<String, Object> exportRequest = new HashMap<>();
            exportRequest.put("format", format);
            exportRequest.put("startDate", startDate != null ? startDate : LocalDate.now().minusDays(30).toString());
            exportRequest.put("endDate", endDate != null ? endDate : LocalDate.now().toString());
            exportRequest.put("type", type);
            
            StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
            if (startDate != null) queryDTO.setStartDate(LocalDate.parse(startDate));
            if (endDate != null) queryDTO.setEndDate(LocalDate.parse(endDate));
            
            OverallStatisticsVO overallStats = statisticsService.getOverallStatistics(queryDTO);
            exportRequest.put("data", overallStats);
            
            byte[] fileBytes = pythonClient.exportStatistics(exportRequest);
            
            String fileExtension = format.equals("pdf") ? "pdf" : (format.equals("csv") ? "csv" : "xlsx");
            String fileName = "统计报表_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "." + fileExtension;
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            
            response.setContentType(getContentType(format));
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);
            response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
            response.getOutputStream().write(fileBytes);
            response.getOutputStream().flush();
            
            log.info("导出统计报表成功：fileName={}", fileName);
        } catch (Exception e) {
            log.error("导出统计报表失败", e);
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"message\":\"导出失败：" + e.getMessage() + "\"}");
            } catch (Exception ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }
    
    @GetMapping("/export/trip")
    public void exportTripReport(
            @RequestParam(value = "format", defaultValue = "excel") String format,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            HttpServletResponse response) {
        log.info("导出行程报表：format={}, startDate={}, endDate={}", format, startDate, endDate);
        
        try {
            Map<String, Object> exportRequest = new HashMap<>();
            exportRequest.put("format", format);
            exportRequest.put("startDate", startDate != null ? startDate : LocalDate.now().minusDays(30).toString());
            exportRequest.put("endDate", endDate != null ? endDate : LocalDate.now().toString());
            
            StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
            if (startDate != null) queryDTO.setStartDate(LocalDate.parse(startDate));
            if (endDate != null) queryDTO.setEndDate(LocalDate.parse(endDate));
            
            List<TripStatisticsVO> tripStats = statisticsService.getTripStatistics(queryDTO);
            exportRequest.put("data", tripStats);
            
            byte[] fileBytes = pythonClient.exportTripReport(exportRequest);
            
            String fileExtension = format.equals("pdf") ? "pdf" : (format.equals("csv") ? "csv" : "xlsx");
            String fileName = "行程报表_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "." + fileExtension;
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            
            response.setContentType(getContentType(format));
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);
            response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
            response.getOutputStream().write(fileBytes);
            response.getOutputStream().flush();
            
            log.info("导出行程报表成功：fileName={}", fileName);
        } catch (Exception e) {
            log.error("导出行程报表失败", e);
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"message\":\"导出失败：" + e.getMessage() + "\"}");
            } catch (Exception ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }
    
    @GetMapping("/export/vehicle")
    public void exportVehicleReport(
            @RequestParam(value = "format", defaultValue = "excel") String format,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            HttpServletResponse response) {
        log.info("导出车辆报表：format={}, startDate={}, endDate={}", format, startDate, endDate);
        
        try {
            Map<String, Object> exportRequest = new HashMap<>();
            exportRequest.put("format", format);
            exportRequest.put("startDate", startDate != null ? startDate : LocalDate.now().minusDays(30).toString());
            exportRequest.put("endDate", endDate != null ? endDate : LocalDate.now().toString());
            
            List<VehicleTripStatsVO> vehicleStats = statisticsService.getVehicleTripStats("day", startDate, endDate);
            exportRequest.put("data", vehicleStats);
            
            byte[] fileBytes = pythonClient.exportVehicleReport(exportRequest);
            
            String fileExtension = format.equals("pdf") ? "pdf" : (format.equals("csv") ? "csv" : "xlsx");
            String fileName = "车辆报表_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "." + fileExtension;
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            
            response.setContentType(getContentType(format));
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);
            response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
            response.getOutputStream().write(fileBytes);
            response.getOutputStream().flush();
            
            log.info("导出车辆报表成功：fileName={}", fileName);
        } catch (Exception e) {
            log.error("导出车辆报表失败", e);
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"message\":\"导出失败：" + e.getMessage() + "\"}");
            } catch (Exception ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }
    
    private String getContentType(String format) {
        switch (format.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "csv":
                return "text/csv;charset=UTF-8";
            default:
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
    }
}
