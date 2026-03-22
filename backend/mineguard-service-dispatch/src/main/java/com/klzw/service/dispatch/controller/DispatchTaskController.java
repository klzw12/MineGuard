package com.klzw.service.dispatch.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.dispatch.dto.DispatchTaskDTO;
import com.klzw.service.dispatch.service.DispatchService;
import com.klzw.service.dispatch.service.DispatchTaskService;
import com.klzw.service.dispatch.vo.DispatchTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/dispatch/task")
@RequiredArgsConstructor
public class DispatchTaskController {

    private final DispatchTaskService dispatchTaskService;
    private final DispatchService dispatchService;

    @PostMapping
    public Result<DispatchTaskVO> create(@RequestBody DispatchTaskDTO dto) {
        return Result.success(dispatchTaskService.create(dto));
    }

    @PutMapping
    public Result<DispatchTaskVO> update(@RequestBody DispatchTaskDTO dto) {
        return Result.success(dispatchTaskService.update(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dispatchTaskService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DispatchTaskVO> getById(@PathVariable Long id) {
        return Result.success(dispatchTaskService.getById(id));
    }

    @GetMapping("/plan/{planId}")
    public Result<List<DispatchTaskVO>> getByPlanId(@PathVariable Long planId) {
        return Result.success(dispatchTaskService.getByPlanId(planId));
    }

    @GetMapping("/vehicle/{vehicleId}/pending")
    public Result<List<DispatchTaskVO>> getPendingByVehicleId(@PathVariable Long vehicleId) {
        return Result.success(dispatchTaskService.getPendingByVehicleId(vehicleId));
    }

    @GetMapping("/driver/{driverId}/pending")
    public Result<List<DispatchTaskVO>> getPendingByDriverId(@PathVariable Long driverId) {
        return Result.success(dispatchTaskService.getPendingByDriverId(driverId));
    }

    @PutMapping("/{taskId}/assign-vehicle")
    public Result<Void> assignVehicle(@PathVariable Long taskId, @RequestParam Long vehicleId) {
        dispatchTaskService.assignVehicle(taskId, vehicleId);
        return Result.success();
    }

    @PutMapping("/{taskId}/assign-driver")
    public Result<Void> assignDriver(@PathVariable Long taskId, @RequestParam Long driverId) {
        dispatchTaskService.assignDriver(taskId, driverId);
        return Result.success();
    }

    @PutMapping("/{taskId}/start")
    public Result<Void> startTask(@PathVariable Long taskId) {
        dispatchTaskService.startTask(taskId);
        return Result.success();
    }

    @PutMapping("/{taskId}/complete")
    public Result<Void> completeTask(@PathVariable Long taskId) {
        dispatchTaskService.completeTask(taskId);
        return Result.success();
    }

    @PutMapping("/{taskId}/cancel")
    public Result<Void> cancelTask(@PathVariable Long taskId) {
        dispatchTaskService.cancelTask(taskId);
        return Result.success();
    }

    @PutMapping("/{taskId}/reassign")
    public Result<Void> reassignTask(
            @PathVariable Long taskId,
            @RequestParam Long newVehicleId,
            @RequestParam Long newDriverId) {
        dispatchTaskService.reassignTask(taskId, newVehicleId, newDriverId);
        return Result.success();
    }

    @GetMapping("/available")
    public Result<List<DispatchTaskVO>> getAvailableTasksForReassignment(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.success(dispatchTaskService.getAvailableTasksForReassignment(startTime, endTime));
    }

    @PostMapping("/dynamic-adjust/vehicle-fault/{vehicleId}")
    public Result<Void> dynamicAdjustForVehicleFault(@PathVariable Long vehicleId) {
        dispatchService.dynamicAdjustForVehicleFault(vehicleId);
        return Result.success();
    }

    @PostMapping("/dynamic-adjust/driver-leave/{driverId}")
    public Result<Void> dynamicAdjustForDriverLeave(@PathVariable Long driverId) {
        dispatchService.dynamicAdjustForDriverLeave(driverId);
        return Result.success();
    }

    @PostMapping("/dynamic-adjust/user-leave")
    public Result<Void> dynamicAdjustForUserLeave(
            @RequestParam Long userId,
            @RequestParam String roleCode) {
        dispatchService.dynamicAdjustForUserLeave(userId, roleCode);
        return Result.success();
    }

    @PostMapping("/dynamic-adjust/route-block/{routeId}")
    public Result<Void> dynamicAdjustForRouteBlock(@PathVariable Long routeId) {
        dispatchService.dynamicAdjustForRouteBlock(routeId);
        return Result.success();
    }
}
