package com.klzw.service.dispatch.controller;

import com.klzw.common.core.domain.dto.TripCreateRequest;
import com.klzw.common.core.result.Result;
import com.klzw.service.dispatch.entity.TransportTask;
import com.klzw.service.dispatch.service.DispatchService;
import com.klzw.service.dispatch.vo.DispatchTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dispatch/main")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    @Operation(summary = "创建调度任务")
    @PostMapping("/task")
    public Result<TransportTask> createDispatchTask(@RequestBody TransportTask task) {
        log.debug("创建调度任务：{}", task);
        return Result.success(dispatchService.createDispatchTask(task));
    }

    @Operation(summary = "更新调度任务")
    @PutMapping("/task/{id}")
    public Result<TransportTask> updateDispatchTask(@PathVariable Long id, @RequestBody TransportTask task) {
        task.setId(id);
        log.debug("更新调度任务：{}", task);
        return Result.success(dispatchService.updateDispatchTask(task));
    }

    @Operation(summary = "删除调度任务")
    @DeleteMapping("/task/{id}")
    public Result<Boolean> deleteDispatchTask(@PathVariable Long id) {
        log.debug("删除调度任务：ID={}", id);
        return Result.success(dispatchService.deleteDispatchTask(id));
    }

    @Operation(summary = "获取调度任务详情")
    @GetMapping("/task/{taskId}")
    public Result<TransportTask> getDispatchTask(@PathVariable Long taskId) {
        log.debug("获取调度任务：ID={}", taskId);
        return Result.success(dispatchService.getDispatchTask(taskId));
    }

    @Operation(summary = "获取调度任务列表")
    @GetMapping("/task/list")
    public Result<List<DispatchTaskVO>> getDispatchTaskList(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String endDate) {
        log.debug("获取调度任务列表：状态={}, 开始日期={}, 结束日期={}", status, startDate, endDate);
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate.replace(" ", "T")) : null;
        LocalDateTime end = null;
        if (endDate != null) {
            end = LocalDateTime.parse(endDate.replace(" ", "T"));
            if (end.getHour() == 0 && end.getMinute() == 0 && end.getSecond() == 0) {
                end = end.withHour(23).withMinute(59).withSecond(59);
            }
        }
        return Result.success(dispatchService.getAllTaskList(status, start, end));
    }

    @Operation(summary = "获取计划下的调度任务")
    @GetMapping("/task/plan/{planId}")
    public Result<List<DispatchTaskVO>> getDispatchTasksByPlanId(@PathVariable Long planId) {
        log.debug("获取计划下的调度任务：planId={}", planId);
        return Result.success(dispatchService.getTasksByPlanId(planId));
    }

    @Operation(summary = "获取车辆待处理任务")
    @GetMapping("/task/vehicle/{vehicleId}/pending")
    public Result<List<TransportTask>> getPendingTasksByVehicle(@PathVariable Long vehicleId) {
        log.debug("获取车辆待处理任务：vehicleId={}", vehicleId);
        return Result.success(dispatchService.getPendingTasksByVehicle(vehicleId));
    }

    @Operation(summary = "获取司机待处理任务")
    @GetMapping("/task/driver/{userId}/pending")
    public Result<List<DispatchTaskVO>> getPendingTasksByDriver(@PathVariable Long userId) {
        log.debug("获取司机待处理任务：userId={}", userId);
        return Result.success(dispatchService.getPendingTasksByDriver(userId));
    }

    @Operation(summary = "执行调度任务")
    @PostMapping("/task/{taskId}/execute")
    public Result<Boolean> executeDispatch(@PathVariable Long taskId) {
        log.debug("执行调度任务：ID={}", taskId);
        return Result.success(dispatchService.executeDispatch(taskId));
    }

    @Operation(summary = "取消调度任务")
    @PostMapping("/task/{taskId}/cancel")
    public Result<Boolean> cancelDispatchTask(@PathVariable Long taskId) {
        log.debug("取消调度任务：ID={}", taskId);
        return Result.success(dispatchService.cancelDispatchTask(taskId));
    }

    @Operation(summary = "重新调度已取消的任务")
    @PostMapping("/task/{taskId}/reschedule")
    public Result<Boolean> rescheduleTask(@PathVariable Long taskId) {
        log.debug("重新调度已取消的任务：ID={}", taskId);
        return Result.success(dispatchService.rescheduleTask(taskId));
    }

    @Operation(summary = "分配车辆")
    @PutMapping("/task/{taskId}/assign-vehicle")
    public Result<Void> assignVehicle(@PathVariable Long taskId, @RequestBody AssignVehicleRequest request) {
        log.debug("分配车辆：taskId={}, vehicleId={}", taskId, request.getVehicleId());
        dispatchService.assignVehicle(taskId, request.getVehicleId());
        return Result.success();
    }

    @Operation(summary = "分配司机")
    @PutMapping("/task/{taskId}/assign-driver")
    public Result<Void> assignDriver(@PathVariable Long taskId, @RequestBody AssignDriverRequest request) {
        log.debug("分配司机：taskId={}, driverId={}", taskId, request.getDriverId());
        dispatchService.assignDriver(taskId, request.getDriverId());
        return Result.success();
    }

    @Operation(summary = "开始任务（由trip模块回调）")
    @PutMapping("/task/{taskId}/start")
    public Result<Void> startTask(@PathVariable Long taskId) {
        log.debug("开始任务（trip模块回调）：ID={}", taskId);
        dispatchService.updateTaskStatusToInProgress(taskId);
        return Result.success();
    }

    @Operation(summary = "完成任务")
    @PutMapping("/task/{taskId}/complete")
    public Result<Void> completeTask(@PathVariable Long taskId) {
        log.debug("完成任务：ID={}", taskId);
        dispatchService.completeTask(taskId);
        return Result.success();
    }

    @Operation(summary = "司机接单")
    @PostMapping("/task/{taskId}/accept")
    public Result<java.util.Map<String, String>> acceptTask(@PathVariable Long taskId) {
        log.debug("司机接单：ID={}", taskId);
        try {
            TransportTask task = dispatchService.getDispatchTask(taskId);
            if (task == null) {
                return Result.fail(910, "任务不存在");
            }
            if (task.getStatus() != 0) {
                return Result.fail(911, "任务状态错误，只能接取待接单状态的任务");
            }
            
            // 更新任务状态为已接单
            task.setStatus(1);
            task.setAcceptTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            dispatchService.updateDispatchTask(task);
            
            // 创建行程
            Long driverId = task.getExecutorId();
            Long vehicleId = task.getVehicleId();
            if (driverId != null && vehicleId != null) {
                try {
                    TripCreateRequest request = new TripCreateRequest();
                    request.setVehicleId(vehicleId);
                    request.setDriverId(driverId);
                    request.setDispatchTaskId(taskId);
                    request.setStartLocation(task.getStartLocation());
                    request.setEndLocation(task.getEndLocation());
                    request.setStartLongitude(task.getStartLongitude());
                    request.setStartLatitude(task.getStartLatitude());
                    request.setEndLongitude(task.getEndLongitude());
                    request.setEndLatitude(task.getEndLatitude());
                    request.setEstimatedStartTime(task.getScheduledStartTime());
                    request.setEstimatedEndTime(task.getScheduledEndTime());
                    request.setTripType(1);
                    request.setRemark("司机接单自动生成，任务编号：" + task.getTaskNo());
                    
                    Long tripId = dispatchService.createTripFromTask(task, driverId, vehicleId);
                    if (tripId != null) {
                        java.util.Map<String, String> result = new java.util.HashMap<>();
                        result.put("tripId", tripId.toString());
                        return Result.success(result);
                    }
                } catch (Exception e) {
                    log.error("创建行程失败：任务 ID={}, 错误={}", taskId, e.getMessage());
                }
            }
            
            return Result.success(new java.util.HashMap<>());
        } catch (Exception e) {
            log.error("司机接单失败：任务 ID={}, 错误={}", taskId, e.getMessage());
            return Result.fail(912, "司机接单失败");
        }
    }

    @Operation(summary = "重新分配任务")
    @PutMapping("/task/{taskId}/reassign")
    public Result<Void> reassignTask(@PathVariable Long taskId, @RequestBody ReassignTaskRequest request) {
        log.debug("重新分配任务：taskId={}, newVehicleId={}, newDriverId={}", taskId, request.getNewVehicleId(), request.getNewDriverId());
        dispatchService.reassignTask(taskId, request.getNewVehicleId(), request.getNewDriverId());
        return Result.success();
    }

    @Operation(summary = "获取可重新分配的任务")
    @GetMapping("/task/available")
    public Result<List<TransportTask>> getAvailableTasksForReassignment(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        log.debug("获取可重新分配的任务：startTime={}, endTime={}", startTime, endTime);
        LocalDateTime start = LocalDateTime.parse(startTime.replace(" ", "T"));
        LocalDateTime end = LocalDateTime.parse(endTime.replace(" ", "T"));
        return Result.success(dispatchService.getAvailableTasksForReassignment(start, end));
    }

    @Operation(summary = "自动生成调度任务")
    @PostMapping("/task/auto-generate")
    public Result<Integer> autoGenerateDispatchTasks() {
        log.debug("自动生成调度任务");
        return Result.success(dispatchService.autoGenerateDispatchTasks());
    }

    @Operation(summary = "车辆故障动态调整")
    @PostMapping("/task/dynamic-adjust/vehicle-fault/{vehicleId}")
    public Result<Void> dynamicAdjustVehicleFault(@PathVariable Long vehicleId) {
        log.info("车辆故障动态调整：车辆ID={}", vehicleId);
        dispatchService.dynamicAdjustForVehicleFault(vehicleId);
        return Result.success();
    }

    @Operation(summary = "司机请假动态调整")
    @PostMapping("/task/dynamic-adjust/driver-leave/{driverId}")
    public Result<Void> dynamicAdjustDriverLeave(@PathVariable Long driverId) {
        log.info("司机请假动态调整：司机ID={}", driverId);
        dispatchService.dynamicAdjustForDriverLeave(driverId);
        return Result.success();
    }

    @Operation(summary = "线路堵塞动态调整")
    @PostMapping("/task/dynamic-adjust/route-block/{routeId}")
    public Result<Void> dynamicAdjustRouteBlock(@PathVariable Long routeId) {
        log.info("线路堵塞动态调整：线路ID={}", routeId);
        dispatchService.dynamicAdjustForRouteBlock(routeId);
        return Result.success();
    }

    @Operation(summary = "用户请假任务重新分配")
    @PostMapping("/task/dynamic-adjust/user-leave")
    public Result<Void> reassignTasksByUserLeave(
            @RequestParam Long userId,
            @RequestParam String roleCode) {
        log.info("用户请假任务重新分配：用户 ID={}, 角色编码={}", userId, roleCode);
        dispatchService.reassignTasksByUserLeave(userId, roleCode);
        return Result.success();
    }

    @Operation(summary = "从故障申报创建维修任务")
    @PostMapping("/maintenance-task/from-fault")
    public Result<Long> createMaintenanceTaskFromFault(@RequestBody java.util.Map<String, Object> faultInfo) {
        log.info("从故障申报创建维修任务：{}", faultInfo);
        Long taskId = dispatchService.createMaintenanceTaskFromFault(faultInfo);
        return Result.success(taskId);
    }

    @lombok.Data
    public static class AssignVehicleRequest {
        private Long vehicleId;
    }

    @lombok.Data
    public static class AssignDriverRequest {
        private Long driverId;
    }

    @lombok.Data
    public static class ReassignTaskRequest {
        private Long newVehicleId;
        private Long newDriverId;
    }
}
