package com.klzw.service.user.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.user.service.DriverService;
import com.klzw.service.user.vo.DriverVehicleVO;
import com.klzw.service.user.vo.DriverVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @GetMapping("/{id}")
    public Result<DriverVO> getById(@PathVariable Long id) {
        return Result.success(driverService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public Result<DriverVO> getByUserId(@PathVariable Long userId) {
        return Result.success(driverService.getByUserId(userId));
    }

    @GetMapping("/list")
    public Result<List<DriverVO>> getList(
            @RequestParam(required = false) String driverName,
            @RequestParam(required = false) Integer status) {
        return Result.success(driverService.getList(driverName, status));
    }

    @PutMapping("/{driverId}/status")
    public Result<Void> updateStatus(
            @PathVariable Long driverId,
            @RequestParam Integer status) {
        driverService.updateStatus(driverId, status);
        return Result.success();
    }

    @GetMapping("/available")
    public Result<List<DriverVO>> getAvailableDrivers() {
        return Result.success(driverService.getAvailableDrivers());
    }

    @GetMapping("/available-repairmen")
    public Result<List<DriverVO>> getAvailableRepairmen() {
        return Result.success(driverService.getAvailableRepairmen());
    }

    @GetMapping("/available-safety-officers")
    public Result<List<DriverVO>> getAvailableSafetyOfficers() {
        return Result.success(driverService.getAvailableSafetyOfficers());
    }

    @PostMapping("/best")
    public Result<DriverVO> selectBestDriver(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String scheduledTime) {
        return Result.success(driverService.selectBestDriver(vehicleId, scheduledTime));
    }

    @PostMapping("/{driverId}/common-vehicle")
    public Result<Void> addCommonVehicle(
            @PathVariable Long driverId,
            @RequestParam Long vehicleId) {
        driverService.addCommonVehicle(driverId, vehicleId);
        return Result.success();
    }

    @DeleteMapping("/{driverId}/common-vehicle/{vehicleId}")
    public Result<Void> removeCommonVehicle(
            @PathVariable Long driverId,
            @PathVariable Long vehicleId) {
        driverService.removeCommonVehicle(driverId, vehicleId);
        return Result.success();
    }

    @PutMapping("/{driverId}/common-vehicle/{vehicleId}/default")
    public Result<Void> setDefaultVehicle(
            @PathVariable Long driverId,
            @PathVariable Long vehicleId) {
        driverService.setDefaultVehicle(driverId, vehicleId);
        return Result.success();
    }

    @GetMapping("/{driverId}/common-vehicles")
    public Result<List<DriverVehicleVO>> getCommonVehicles(@PathVariable Long driverId) {
        return Result.success(driverService.getCommonVehicles(driverId));
    }

    @GetMapping("/ids")
    public Result<List<Long>> getDriverIds() {
        return Result.success(driverService.getDriverIds());
    }
}
