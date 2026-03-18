package com.klzw.service.vehicle.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.vehicle.dto.VehicleRefuelingDTO;
import com.klzw.service.vehicle.entity.VehicleRefueling;
import com.klzw.service.vehicle.service.VehicleRefuelingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "车辆加油管理", description = "车辆加油管理接口")
@RestController
@RequestMapping("/api/vehicle/refueling")
@RequiredArgsConstructor
public class VehicleRefuelingController {
    
    private final VehicleRefuelingService vehicleRefuelingService;
    
    @Operation(summary = "添加加油记录")
    @PostMapping
    public Result<VehicleRefueling> addRefuelingRecord(@RequestBody VehicleRefuelingDTO refuelingDTO) {
        VehicleRefueling refueling = vehicleRefuelingService.addRefuelingRecord(refuelingDTO);
        return Result.success(refueling);
    }
    
    @Operation(summary = "获取车辆加油记录")
    @GetMapping("/vehicle/{vehicleId}")
    public Result<List<VehicleRefueling>> getRefuelingRecords(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<VehicleRefueling> records = vehicleRefuelingService.getRefuelingRecords(vehicleId, page, size);
        return Result.success(records);
    }
    
}