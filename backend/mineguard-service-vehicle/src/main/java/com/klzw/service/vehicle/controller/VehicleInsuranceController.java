package com.klzw.service.vehicle.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.vehicle.dto.VehicleInsuranceDTO;
import com.klzw.service.vehicle.entity.VehicleInsurance;
import com.klzw.service.vehicle.service.VehicleInsuranceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "车辆保险管理", description = "车辆保险管理接口")
@RestController
@RequestMapping("/vehicle/insurance")
@RequiredArgsConstructor
public class VehicleInsuranceController {
    
    private final VehicleInsuranceService vehicleInsuranceService;
    
    @Operation(summary = "添加保险信息")
    @PostMapping
    public Result<VehicleInsurance> addInsurance(@RequestBody VehicleInsuranceDTO insuranceDTO) {
        VehicleInsurance insurance = vehicleInsuranceService.addInsurance(insuranceDTO);
        return Result.success(insurance);
    }
    
    @Operation(summary = "获取所有保险记录")
    @GetMapping("/list")
    public Result<List<VehicleInsurance>> getAllInsuranceRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<VehicleInsurance> records = vehicleInsuranceService.getAllInsuranceRecords(page, size);
        return Result.success(records);
    }
    
    @Operation(summary = "获取车辆保险信息")
    @GetMapping("/vehicle/{vehicleId}")
    public Result<List<VehicleInsurance>> getVehicleInsurance(@PathVariable Long vehicleId) {
        List<VehicleInsurance> insurances = vehicleInsuranceService.getVehicleInsurance(vehicleId);
        return Result.success(insurances);
    }
    
    @Operation(summary = "获取当前有效保险信息")
    @GetMapping("/vehicle/{vehicleId}/current")
    public Result<VehicleInsurance> getCurrentInsurance(@PathVariable Long vehicleId) {
        VehicleInsurance insurance = vehicleInsuranceService.getCurrentInsurance(vehicleId);
        return Result.success(insurance);
    }
    
}