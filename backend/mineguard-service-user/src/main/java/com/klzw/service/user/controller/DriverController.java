package com.klzw.service.user.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.user.service.DriverScoreService;
import com.klzw.service.user.service.DriverService;
import com.klzw.service.user.vo.DriverVehicleVO;
import com.klzw.service.user.vo.DriverVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;
    private final DriverScoreService driverScoreService;

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

    @PostMapping("/user/{userId}/common-vehicle")
    public Result<Void> addCommonVehicleByUserId(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body) {
        Long vehicleId = body.get("vehicleId") != null ? Long.valueOf(body.get("vehicleId").toString()) : null;
        driverService.addCommonVehicleByUserId(userId, vehicleId);
        return Result.success();
    }

    @DeleteMapping("/user/{userId}/common-vehicle/{vehicleId}")
    public Result<Void> removeCommonVehicleByUserId(
            @PathVariable Long userId,
            @PathVariable Long vehicleId) {
        driverService.removeCommonVehicleByUserId(userId, vehicleId);
        return Result.success();
    }

    @PutMapping("/user/{userId}/common-vehicle/{vehicleId}/default")
    public Result<Void> setDefaultVehicleByUserId(
            @PathVariable Long userId,
            @PathVariable Long vehicleId) {
        driverService.setDefaultVehicleByUserId(userId, vehicleId);
        return Result.success();
    }

    @GetMapping("/user/{userId}/common-vehicles")
    public Result<List<DriverVehicleVO>> getCommonVehiclesByUserId(@PathVariable Long userId) {
        return Result.success(driverService.getCommonVehiclesByUserId(userId));
    }

    @PostMapping("/{driverId}/common-vehicle")
    public Result<Void> addCommonVehicle(
            @PathVariable Long driverId,
            @RequestBody Map<String, Object> body) {
        Long vehicleId = body.get("vehicleId") != null ? Long.valueOf(body.get("vehicleId").toString()) : null;
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

    @PutMapping("/{driverId}/team")
    public Result<Void> updateBelongingTeam(
            @PathVariable Long driverId,
            @RequestBody Map<String, String> body) {
        String belongingTeam = body.get("belongingTeam");
        driverService.updateBelongingTeam(driverId, belongingTeam);
        return Result.success();
    }

    @PutMapping("/user/{userId}/team")
    public Result<Void> updateBelongingTeamByUserId(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body) {
        String belongingTeam = body.get("belongingTeam");
        driverService.updateBelongingTeamByUserId(userId, belongingTeam);
        return Result.success();
    }

    @PostMapping("/score/update-from-trip")
    @Operation(summary = "根据行程更新司机分数（内部调用）")
    public Result<Integer> updateDriverScoreFromTrip(
            @RequestParam Long driverId,
            @RequestParam Integer pythonScore,
            @RequestParam Double tripDistance) {
        int tripScore = driverScoreService.updateScoreFromTrip(driverId, pythonScore, tripDistance);
        return Result.success(tripScore);
    }
}