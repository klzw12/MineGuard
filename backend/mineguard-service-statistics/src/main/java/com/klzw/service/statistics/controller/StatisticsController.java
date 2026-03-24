package com.klzw.service.statistics.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.statistics.dto.StatisticsQueryDTO;
import com.klzw.service.statistics.service.StatisticsService;
import com.klzw.service.statistics.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

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

    @GetMapping("/vehicle")
    public Result<List<VehicleStatisticsVO>> getVehicleStatistics(StatisticsQueryDTO queryDTO) {
        log.debug("获取车辆统计数据：{}", queryDTO);
        List<VehicleStatisticsVO> list = statisticsService.getVehicleStatistics(queryDTO);
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
            @RequestParam("date") String date) {
        log.debug("计算车辆统计数据：车辆ID={}, 日期={}", vehicleId, date);
        VehicleStatisticsVO vo = statisticsService.calculateVehicleStatistics(vehicleId, date);
        return Result.success(vo);
    }

    @PostMapping("/driver/calculate")
    public Result<DriverStatisticsVO> calculateDriverStatistics(
            @RequestParam("userId") Long userId,
            @RequestParam("date") String date) {
        log.debug("计算司机统计数据：用户 ID={}, 日期={}", userId, date);
        DriverStatisticsVO vo = statisticsService.calculateDriverStatistics(userId, date);
        return Result.success(vo);
    }
    
    @GetMapping("/fault")
    public Result<FaultStatisticsVO> getFaultStatistics(StatisticsQueryDTO queryDTO) {
        log.debug("获取故障统计数据：{}", queryDTO);
        FaultStatisticsVO vo = statisticsService.getFaultStatistics(queryDTO);
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
            @RequestParam("date") String date) {
        log.debug("计算故障统计数据：车辆 ID={}, 日期={}", vehicleId, date);
        statisticsService.calculateFaultStatistics(vehicleId, date);
        return Result.success();
    }
    
    @PostMapping("/transport/calculate")
    public Result<TransportStatisticsVO> calculateTransportStatistics(@RequestParam("date") String date) {
        log.debug("计算运输统计数据：日期={}", date);
        TransportStatisticsVO vo = statisticsService.calculateTransportStatistics(date);
        return Result.success(vo);
    }
}
