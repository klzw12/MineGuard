package com.klzw.service.vehicle.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.vehicle.dto.VehicleMaintenanceDTO;
import com.klzw.service.vehicle.entity.VehicleMaintenance;
import com.klzw.service.vehicle.service.VehicleMaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "车辆保养管理", description = "车辆保养管理接口")
@RestController
@RequestMapping("/vehicle/maintenance")
@RequiredArgsConstructor
public class VehicleMaintenanceController {
    
    private final VehicleMaintenanceService vehicleMaintenanceService;
    
    @Operation(summary = "添加保养记录")
    @PostMapping
    public Result<VehicleMaintenance> addMaintenanceRecord(@RequestBody VehicleMaintenanceDTO maintenanceDTO) {
        VehicleMaintenance maintenance = vehicleMaintenanceService.addMaintenanceRecord(maintenanceDTO);
        return Result.success(maintenance);
    }
    
    @Operation(summary = "获取车辆保养记录")
    @GetMapping("/vehicle/{vehicleId}")
    public Result<List<VehicleMaintenance>> getMaintenanceRecords(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<VehicleMaintenance> records = vehicleMaintenanceService.getMaintenanceRecords(vehicleId, page, size);
        return Result.success(records);
    }
    
    @Operation(summary = "获取下次保养信息")
    @GetMapping("/vehicle/{vehicleId}/next")
    public Result<VehicleMaintenance> getNextMaintenance(@PathVariable Long vehicleId) {
        VehicleMaintenance maintenance = vehicleMaintenanceService.getNextMaintenance(vehicleId);
        return Result.success(maintenance);
    }
    
}