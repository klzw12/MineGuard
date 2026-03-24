package com.klzw.service.vehicle.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.vehicle.dto.VehicleStatusReportDTO;
import com.klzw.service.vehicle.entity.VehicleStatus;
import com.klzw.service.vehicle.service.VehicleStatusService;
import com.klzw.service.vehicle.vo.VehicleStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "车辆状态", description = "车辆状态接口")
@RestController
@RequestMapping("/vehicle")
@RequiredArgsConstructor
public class VehicleStatusController {
    
    private final VehicleStatusService vehicleStatusService;
    
    @Operation(summary = "获取实时状态")
    @GetMapping("/{id}/status")
    public Result<VehicleStatusVO> getRealTimeStatus(@PathVariable Long id) {
        VehicleStatusVO status = vehicleStatusService.getRealTimeStatus(id);
        return Result.success(status);
    }
    
    @Operation(summary = "更新状态")
    @PutMapping("/{id}/status")
    public Result<VehicleStatus> updateStatus(@PathVariable Long id, @RequestBody VehicleStatus status) {
        VehicleStatus updatedStatus = vehicleStatusService.updateStatus(id, status);
        return Result.success(updatedStatus);
    }
    
    @Operation(summary = "获取状态历史")
    @GetMapping("/{id}/status/history")
    public Result<List<VehicleStatusVO>> getStatusHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<VehicleStatusVO> history = vehicleStatusService.getStatusHistory(id, page, size);
        return Result.success(history);
    }
    
    @Operation(summary = "上报车辆状态")
    @PostMapping("/{id}/status/report")
    public Result<Void> reportStatus(@PathVariable Long id, @RequestBody VehicleStatusReportDTO reportDTO) {
        reportDTO.setVehicleId(id);
        vehicleStatusService.reportStatus(reportDTO);
        return Result.success();
    }
}
