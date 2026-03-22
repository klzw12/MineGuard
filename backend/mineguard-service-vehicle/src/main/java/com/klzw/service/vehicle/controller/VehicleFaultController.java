package com.klzw.service.vehicle.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.vehicle.dto.VehicleFaultDTO;
import com.klzw.service.vehicle.dto.VehicleFaultStatisticsResponseDTO;
import com.klzw.service.vehicle.entity.VehicleFault;
import com.klzw.service.vehicle.service.VehicleFaultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "车辆故障管理", description = "车辆故障管理接口")
@RestController
@RequestMapping("/api/vehicle/fault")
@RequiredArgsConstructor
public class VehicleFaultController {
    
    private final VehicleFaultService vehicleFaultService;
    
    @Operation(summary = "报告故障")
    @PostMapping
    public Result<VehicleFault> reportFault(@RequestBody VehicleFaultDTO faultDTO) {
        VehicleFault fault = vehicleFaultService.reportFault(faultDTO);
        return Result.success(fault);
    }
    
    @Operation(summary = "处理故障")
    @PutMapping("/{id}/handle")
    public Result<VehicleFault> handleFault(
            @PathVariable Long id,
            @RequestParam Long repairmanId,
            @RequestParam String repairContent,
            @RequestParam java.math.BigDecimal repairCost) {
        VehicleFault fault = vehicleFaultService.handleFault(id, repairmanId, repairContent, repairCost);
        return Result.success(fault);
    }
    
    @Operation(summary = "获取车辆故障记录")
    @GetMapping("/vehicle/{vehicleId}")
    public Result<List<VehicleFault>> getFaultRecords(
            @PathVariable Long vehicleId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<VehicleFault> records = vehicleFaultService.getFaultRecords(vehicleId, status, page, size);
        return Result.success(records);
    }
    
    @Operation(summary = "按日期范围查询故障统计（供 statistics 服务调用）")
    @GetMapping("/statistics")
    public Result<VehicleFaultStatisticsResponseDTO> getFaultStatistics(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        return Result.success(vehicleFaultService.getFaultStatistics(startDate, endDate));
    }
    
}