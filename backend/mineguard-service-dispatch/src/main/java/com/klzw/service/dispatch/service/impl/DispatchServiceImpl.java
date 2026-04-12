package com.klzw.service.dispatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.core.client.DriverClient;
import com.klzw.common.core.client.MessageClient;
import com.klzw.common.core.client.UserClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.client.TripClient;
import com.klzw.common.core.domain.dto.DriverInfo;
import com.klzw.common.core.domain.dto.DriverVehicleInfo;
import com.klzw.common.core.domain.dto.VehicleInfo;
import com.klzw.common.core.domain.dto.TripCreateRequest;
import com.klzw.common.core.result.Result;
import com.klzw.service.dispatch.constant.DispatchResultCode;
import com.klzw.service.dispatch.entity.TransportTask;
import com.klzw.service.dispatch.entity.RouteTemplate;
import com.klzw.service.dispatch.entity.MaintenanceTask;
import com.klzw.service.dispatch.entity.InspectionTask;
import com.klzw.service.dispatch.exception.DispatchException;
import com.klzw.service.dispatch.mapper.TransportTaskMapper;
import com.klzw.service.dispatch.mapper.RouteTemplateMapper;
import com.klzw.service.dispatch.mapper.MaintenanceTaskMapper;
import com.klzw.service.dispatch.mapper.InspectionTaskMapper;
import com.klzw.service.dispatch.service.DispatchService;
import com.klzw.service.dispatch.vo.DispatchTaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchServiceImpl implements DispatchService {

    private final TransportTaskMapper transportTaskMapper;
    private final RouteTemplateMapper routeTemplateMapper;
    private final MaintenanceTaskMapper maintenanceTaskMapper;
    private final InspectionTaskMapper inspectionTaskMapper;
    private final DriverClient driverClient;
    private final VehicleClient vehicleClient;
    private final UserClient userClient;
    private final MessageClient messageClient;
    private final TripClient tripClient;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean executeDispatch(Long taskId) {
        log.info("执行智能调度：任务 ID={}", taskId);
        
        TransportTask task = transportTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("调度任务不存在：ID={}", taskId);
            throw new DispatchException(DispatchResultCode.TASK_NOT_FOUND);
        }

        DriverInfo bestDriver = selectBestDriverByPriority(task, null);
        if (bestDriver == null) {
            log.error("无可用司机：任务 ID={}", taskId);
            throw new DispatchException(DispatchResultCode.NO_AVAILABLE_DRIVER, "暂无可用司机，请稍后重试");
        }
        
        Long bestVehicleId = selectVehicleByDriverCommonVehicles(bestDriver.getUserId(), task);
        if (bestVehicleId == null) {
            bestVehicleId = selectBestVehicle(task);
            if (bestVehicleId == null) {
                log.error("无可用车辆：任务 ID={}", taskId);
                throw new DispatchException(DispatchResultCode.NO_AVAILABLE_VEHICLE, "暂无可用车辆，请稍后重试");
            }
        }
        
        task.setExecutorId(bestDriver.getUserId());
        task.setVehicleId(bestVehicleId);
        task.setStatus(2);
        task.setActualStartTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        transportTaskMapper.updateById(task);
        
        log.info("智能调度完成：任务 ID={}, 司机 ID={}, 用户 ID={}, 车辆 ID={}", taskId, bestDriver.getId(), bestDriver.getUserId(), bestVehicleId);
        
        try {
            createTripFromTask(task, bestDriver.getId(), bestVehicleId);
        } catch (Exception e) {
            log.error("创建行程失败：任务 ID={}, 错误={}", taskId, e.getMessage());
        }
        
        return true;
    }
    
    /**
     * 根据调度任务创建行程
     */
    public Long createTripFromTask(TransportTask task, Long driverId, Long vehicleId) {
        log.info("根据调度任务创建行程：任务 ID={}, 司机 ID={}, 车辆 ID={}", task.getId(), driverId, vehicleId);
        
        try {
            TripCreateRequest request = new TripCreateRequest();
            request.setVehicleId(vehicleId);
            request.setDriverId(driverId);
            request.setDispatchTaskId(task.getId());
            request.setStartLocation(task.getStartLocation());
            request.setEndLocation(task.getEndLocation());
            request.setStartLongitude(task.getStartLongitude());
            request.setStartLatitude(task.getStartLatitude());
            request.setEndLongitude(task.getEndLongitude());
            request.setEndLatitude(task.getEndLatitude());
            request.setEstimatedStartTime(task.getScheduledStartTime());
            request.setEstimatedEndTime(task.getScheduledEndTime());
            request.setTripType(1);
            request.setRemark("调度任务自动生成，任务编号：" + task.getTaskNo());
            
            var result = tripClient.createTrip(request).block();
            Long tripId = null;
            
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                tripId = result.getData();
                log.info("行程创建成功：任务 ID={}, 行程 ID={}", task.getId(), tripId);
                // 回写tripId到调度任务
                task.setTripId(tripId);
                transportTaskMapper.updateById(task);
            } else {
                log.warn("行程创建失败：任务 ID={}, 错误码={}, 错误信息={}", 
                    task.getId(), result != null ? result.getCode() : null, result != null ? result.getMessage() : null);
            }
            return tripId;
        } catch (Exception e) {
            log.error("调用 TripClient 创建行程失败：{}", e.getMessage());
            throw e;
        }
    }

    private DriverInfo selectBestDriverByPriority(TransportTask task, Long excludeDriverId) {
        String priority = task.getPriority();
        
        try {
            // 1. 获取所有在职司机
            Result<List<DriverInfo>> driversResult = driverClient.getAvailableDrivers();
            List<DriverInfo> drivers = driversResult != null && driversResult.getData() != null ? driversResult.getData() : Collections.emptyList();
            
            if (drivers.isEmpty()) {
                log.warn("没有可用司机");
                return null;
            }
            
            // 2. 排除已分配待接单的司机
            List<Long> assignedDriverIds = transportTaskMapper.findAssignedButNotAcceptedDriverIds();
            log.info("已分配待接单的司机ID列表: {}", assignedDriverIds);
            
            // 3. 排除当前请假的司机
            List<Long> leaveUserIds = getLeaveUserIds();
            log.info("请假用户ID列表: {}", leaveUserIds);
            
            List<DriverInfo> filteredDrivers = drivers.stream()
                .filter(d -> !assignedDriverIds.contains(d.getId()))
                .filter(d -> excludeDriverId == null || !d.getId().equals(excludeDriverId))
                .filter(d -> !leaveUserIds.contains(d.getUserId()))
                .collect(Collectors.toList());
            
            if (filteredDrivers.isEmpty()) {
            log.info("排除已分配待接单和请假司机后无可用司机，放宽条件从所有在职司机中选择");
            filteredDrivers = drivers.stream()
                    .filter(d -> !assignedDriverIds.contains(d.getId()))
                    .filter(d -> excludeDriverId == null || !d.getId().equals(excludeDriverId))
                    .filter(d -> !leaveUserIds.contains(d.getUserId()))
                    .toList();
        }
            
            if (filteredDrivers.isEmpty()) {
                log.warn("无可用司机");
                return null;
            }
            
            // 4. 按照分数排序
            List<DriverInfo> sortedDrivers = filteredDrivers.stream()
                .sorted(Comparator.comparing(DriverInfo::getScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
            
            if (sortedDrivers.isEmpty()) {
                log.warn("无可用司机");
                return null;
            }
            
            // 5. 根据任务优先级确定选择范围
            int totalDrivers = sortedDrivers.size();
            int selectionRange = getSelectionRangeByPriority(priority, totalDrivers);
            
            List<DriverInfo> candidateDrivers = sortedDrivers.stream()
                .limit(selectionRange)
                .toList();
            
            // 6. 优先选择有常用车辆的司机
            if (task.getVehicleId() != null) {
                for (DriverInfo driver : candidateDrivers) {
                    if (hasCommonVehicle(driver.getUserId(), task.getVehicleId())) {
                        log.info("选择常用车辆匹配的司机：司机ID={}, 用户ID={}, 分数={}, 优先级={}", 
                            driver.getId(), driver.getUserId(), driver.getScore(), priority);
                        return driver;
                    }
                }
            }
            
            // 7. 选择最佳司机
            DriverInfo bestDriver = candidateDrivers.getFirst();
            log.info("选择最佳司机：司机ID={}, 用户ID={}, 分数={}, 优先级={}, 候选范围={}/{}", 
                bestDriver.getId(), bestDriver.getUserId(), bestDriver.getScore(), priority, selectionRange, totalDrivers);
            return bestDriver;
            
        } catch (Exception e) {
            log.error("获取可用司机失败：{}", e.getMessage());
        }
        
        return null;
    }

    private int getSelectionRangeByPriority(String priority, int totalDrivers) {
        if (priority == null) return totalDrivers;
        
        switch (priority.toLowerCase()) {
            case "high":
            case "urgent":
                return Math.max(1, (int) Math.ceil(totalDrivers / 3.0));
            case "normal":
                return Math.max(1, (int) Math.ceil(totalDrivers * 2 / 3.0));
            case "low":
            default:
                return totalDrivers;
        }
    }

    private Long selectVehicleByDriverCommonVehicles(Long driverId, TransportTask task) {
        try {
            Result<List<DriverVehicleInfo>> result = driverClient.getCommonVehicles(driverId);
            List<DriverVehicleInfo> commonVehicles = result != null && result.getData() != null ? result.getData() : Collections.emptyList();
            
            if (!commonVehicles.isEmpty()) {
                for (DriverVehicleInfo vehicle : commonVehicles) {
                    if (isVehicleAvailable(vehicle.getVehicleId())) {
                        log.info("选择司机常用车辆：司机ID={}, 车辆ID={}", driverId, vehicle.getVehicleId());
                        return vehicle.getVehicleId();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取司机常用车辆失败：{}", e.getMessage());
        }
        
        return null;
    }

    private boolean hasCommonVehicle(Long driverId, Long vehicleId) {
        try {
            Result<List<DriverVehicleInfo>> result = driverClient.getCommonVehicles(driverId);
            List<DriverVehicleInfo> vehicles = result != null && result.getData() != null ? result.getData() : Collections.emptyList();
            return vehicles.stream()
                .anyMatch(v -> vehicleId.equals(v.getVehicleId()));
        } catch (Exception e) {
            log.warn("检查常用车辆失败：{}", e.getMessage());
        }
        return false;
    }

    private boolean isVehicleAvailable(Long vehicleId) {
        try {
            var result = vehicleClient.getById(vehicleId);
            if (result == null || result.getData() == null || result.getData().getStatus() == null || result.getData().getStatus() != 0) {
                return false;
            }
            
            List<Long> assignedVehicleIds = transportTaskMapper.findAssignedButNotAcceptedVehicleIds();
            if (assignedVehicleIds.contains(vehicleId)) {
                log.info("车辆已分配给待接单任务，不可用：车辆ID={}", vehicleId);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.warn("检查车辆状态失败：车辆ID={}", vehicleId);
        }
        return false;
    }

    private Long selectBestVehicle(TransportTask task) {
        return selectBestVehicle(task, task.getExecutorId());
    }

    private Long selectBestVehicle(TransportTask task, Long driverId) {
        try {
            var result = vehicleClient.selectBestVehicle(
                driverId,
                task.getStartLongitude() != null ? new java.math.BigDecimal(task.getStartLongitude().toString()) : null,
                task.getStartLatitude() != null ? new java.math.BigDecimal(task.getStartLatitude().toString()) : null,
                task.getCargoWeight(),
                task.getScheduledStartTime() != null ? task.getScheduledStartTime().toString() : null
            );
            
            List<VehicleInfo> vehicles = result != null ? result.getData() : null;
            if (vehicles == null || vehicles.isEmpty()) {
                log.warn("没有可用车辆");
                return null;
            }
            
            List<Long> assignedVehicleIds = transportTaskMapper.findAssignedButNotAcceptedVehicleIds();
            log.info("已分配待接单的车辆ID列表: {}", assignedVehicleIds);
            
            for (VehicleInfo vehicle : vehicles) {
                if (vehicle.getId() != null && !assignedVehicleIds.contains(vehicle.getId())) {
                    log.info("选择最佳车辆：车辆ID={}", vehicle.getId());
                    return vehicle.getId();
                }
            }
            
            log.warn("所有车辆都已分配待接单任务");
            return null;
        } catch (Exception e) {
            log.warn("调用车辆服务获取最佳车辆失败：{}", e.getMessage());
        }
        
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dynamicAdjustForVehicleFault(Long vehicleId) {
        log.info("车辆故障动态调整：车辆ID={}", vehicleId);
        
        List<TransportTask> pendingTasks = transportTaskMapper.findPendingByVehicleId(vehicleId);
        
        for (TransportTask task : pendingTasks) {
            try {
                Long newVehicleId = selectBestVehicle(task, task.getExecutorId());
                if (newVehicleId != null) {
                    task.setVehicleId(newVehicleId);
                    task.setUpdateTime(LocalDateTime.now());
                    transportTaskMapper.updateById(task);
                    log.info("任务重新分配车辆：任务 ID={}, 新车辆 ID={}", task.getId(), newVehicleId);
                    
                    // 推送通知给新执行人
                    if (task.getExecutorId() != null) {
                        sendTaskNotification(task.getExecutorId(), task.getId(), task.getTaskNo(), "车辆故障调整");
                    }
                } else {
                    task.setStatus(4);
                    task.setRemark("车辆故障，无可用车辆，自动取消");
                    task.setUpdateTime(LocalDateTime.now());
                    transportTaskMapper.updateById(task);
                    log.warn("任务因无可用车辆取消：任务 ID={}", task.getId());
                }
            } catch (Exception e) {
                log.error("动态调整失败：任务ID={}, 错误={}", task.getId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dynamicAdjustForDriverLeave(Long driverId) {
        log.info("司机请假动态调整：司机ID={}", driverId);
        
        List<TransportTask> pendingTasks = transportTaskMapper.findPendingByExecutorId(driverId);
        
        for (TransportTask task : pendingTasks) {
            try {
                DriverInfo newDriver = selectBestDriverByPriority(task, driverId);
                if (newDriver != null) {
                    task.setExecutorId(newDriver.getUserId());
                    task.setUpdateTime(LocalDateTime.now());
                    transportTaskMapper.updateById(task);
                    log.info("任务重新分配司机：任务 ID={}, 新司机 ID={}, 新用户 ID={}", task.getId(), newDriver.getId(), newDriver.getUserId());
                    
                    // 推送通知给新司机（使用用户ID）
                    sendTaskNotification(newDriver.getUserId(), task.getId(), task.getTaskNo(), "司机请假调整");
                } else {
                    task.setStatus(4);
                    task.setRemark("司机请假，无可用司机，自动取消");
                    task.setUpdateTime(LocalDateTime.now());
                    transportTaskMapper.updateById(task);
                    log.warn("任务因无可用司机取消：任务 ID={}", task.getId());
                }
            } catch (Exception e) {
                log.error("动态调整失败：任务ID={}, 错误={}", task.getId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dynamicAdjustForUserLeave(Long userId, String roleCode) {
        log.info("用户请假动态调整：用户 ID={}, 角色编码={}", userId, roleCode);
        
        if (userId == null || roleCode == null) {
            log.error("用户 ID 或角色编码为空");
            return;
        }
        
        try {
            switch (roleCode) {
                case "DRIVER" -> dynamicAdjustForDriverLeave(userId);
                case "REPAIRMAN" -> dynamicAdjustForRepairmanLeave(userId);
                case "SAFETY_OFFICER" -> dynamicAdjustForSafetyOfficerLeave(userId);
                default -> log.warn("未知角色编码：{}", roleCode);
            }
        } catch (Exception e) {
            log.error("用户请假动态调整失败：用户 ID={}, 角色编码={}, 错误={}", userId, roleCode, e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dynamicAdjustForRepairmanLeave(Long repairmanId) {
        log.info("维修员请假动态调整：维修员 ID={}", repairmanId);
        
        List<MaintenanceTask> pendingTasks = maintenanceTaskMapper.findPendingByExecutorId(repairmanId);
        
        for (MaintenanceTask task : pendingTasks) {
            try {
                Long newRepairmanId = selectAvailableRepairman(task);
                if (newRepairmanId != null) {
                    task.setExecutorId(newRepairmanId);
                    task.setUpdateTime(LocalDateTime.now());
                    maintenanceTaskMapper.updateById(task);
                    log.info("维修任务重新分配维修员：任务 ID={}, 新维修员 ID={}", task.getId(), newRepairmanId);
                    
                    sendMaintenanceTaskNotification(newRepairmanId, task.getId(), task.getTaskNo(), "维修员请假调整");
                } else {
                    task.setStatus(4);
                    task.setRemark("维修员请假，无可用维修员，自动取消");
                    task.setUpdateTime(LocalDateTime.now());
                    maintenanceTaskMapper.updateById(task);
                    log.warn("维修任务因无可用维修员取消：任务 ID={}", task.getId());
                }
            } catch (Exception e) {
                log.error("维修任务动态调整失败：任务 ID={}, 错误={}", task.getId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dynamicAdjustForSafetyOfficerLeave(Long safetyOfficerId) {
        log.info("安全员请假动态调整：安全员 ID={}", safetyOfficerId);
        
        List<InspectionTask> pendingTasks = inspectionTaskMapper.findPendingByExecutorId(safetyOfficerId);
        
        for (InspectionTask task : pendingTasks) {
            try {
                Long newSafetyOfficerId = selectAvailableSafetyOfficer(task);
                if (newSafetyOfficerId != null) {
                    task.setExecutorId(newSafetyOfficerId);
                    task.setUpdateTime(LocalDateTime.now());
                    inspectionTaskMapper.updateById(task);
                    log.info("巡检任务重新分配安全员：任务 ID={}, 新安全员 ID={}", task.getId(), newSafetyOfficerId);
                    
                    sendInspectionTaskNotification(newSafetyOfficerId, task.getId(), task.getTaskNo(), "安全员请假调整");
                } else {
                    task.setStatus(4);
                    task.setRemark("安全员请假，无可用安全员，自动取消");
                    task.setUpdateTime(LocalDateTime.now());
                    inspectionTaskMapper.updateById(task);
                    log.warn("巡检任务因无可用安全员取消：任务 ID={}", task.getId());
                }
            } catch (Exception e) {
                log.error("巡检任务动态调整失败：任务 ID={}, 错误={}", task.getId(), e.getMessage());
            }
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reassignTasksByUserLeave(Long userId, String roleCode) {
        log.info("用户请假任务重新分配（仅未接单任务）：用户 ID={}, 角色编码={}", userId, roleCode);
        
        if (userId == null || roleCode == null) {
            log.error("用户 ID 或角色编码为空");
            return;
        }
        
        try {
            if ("ROLE_DRIVER".equals(roleCode)) {
                reassignTransportTasksByUserLeave(userId);
            } else if ("ROLE_REPAIRMAN".equals(roleCode)) {
                reassignMaintenanceTasksByUserLeave(userId);
            } else if ("ROLE_SAFETY_OFFICER".equals(roleCode)) {
                reassignInspectionTasksByUserLeave(userId);
            } else {
                log.warn("未知角色编码：{}", roleCode);
            }
        } catch (Exception e) {
            log.error("用户请假任务重新分配失败：用户 ID={}, 角色编码={}, 错误={}", userId, roleCode, e.getMessage());
            throw e;
        }
    }
    
    private void reassignTransportTasksByUserLeave(Long driverId) {
        List<TransportTask> unassignedTasks = transportTaskMapper.findUnassignedByExecutorId(driverId);
        
        for (TransportTask task : unassignedTasks) {
            try {
                log.info("重新分配运输任务：任务 ID={}, 任务编号={}", task.getId(), task.getTaskNo());
                
                DriverInfo newDriver = selectBestDriverByPriority(task, driverId);
                if (newDriver != null) {
                    task.setExecutorId(newDriver.getUserId());
                    
                    Long newVehicleId = selectVehicleByDriverCommonVehicles(newDriver.getUserId(), task);
                    if (newVehicleId == null) {
                        newVehicleId = selectBestVehicle(task, newDriver.getId());
                    }
                    task.setVehicleId(newVehicleId);

                    task.setStatus(0);
                    task.setUpdateTime(LocalDateTime.now());
                    transportTaskMapper.updateById(task);

                    log.info("运输任务重新分配完成：任务 ID={}, 新司机 ID={}, 新用户 ID={}, 新车辆 ID={}", 
                        task.getId(), newDriver.getId(), newDriver.getUserId(), newVehicleId);
                    
                    // 推送通知给新司机（使用用户ID）
                    sendTaskNotification(newDriver.getUserId(), task.getId(), task.getTaskNo(), "司机请假重新分配");
                } else {
                    log.warn("运输任务无可用司机：任务 ID={}", task.getId());
                }
            } catch (Exception e) {
                log.error("运输任务重新分配失败：任务 ID={}, 错误={}", task.getId(), e.getMessage());
            }
        }
    }
    
    private void reassignMaintenanceTasksByUserLeave(Long repairmanId) {
        List<MaintenanceTask> unassignedTasks = maintenanceTaskMapper.findUnassignedByExecutorId(repairmanId);
        
        for (MaintenanceTask task : unassignedTasks) {
            try {
                log.info("重新分配维修任务：任务 ID={}, 任务编号={}", task.getId(), task.getTaskNo());
                
                Long newRepairmanId = selectAvailableRepairman(task);
                if (newRepairmanId != null) {
                    task.setExecutorId(newRepairmanId);
                    task.setStatus(0);
                    task.setUpdateTime(LocalDateTime.now());
                    maintenanceTaskMapper.updateById(task);
                    
                    log.info("维修任务重新分配完成：任务 ID={}, 新维修员 ID={}", task.getId(), newRepairmanId);
                    
                    sendMaintenanceTaskNotification(newRepairmanId, task.getId(), task.getTaskNo(), "维修员请假重新分配");
                } else {
                    log.warn("维修任务无可用维修员：任务 ID={}", task.getId());
                }
            } catch (Exception e) {
                log.error("维修任务重新分配失败：任务 ID={}, 错误={}", task.getId(), e.getMessage());
            }
        }
    }
    
    private void reassignInspectionTasksByUserLeave(Long safetyOfficerId) {
        List<InspectionTask> unassignedTasks = inspectionTaskMapper.findUnassignedByExecutorId(safetyOfficerId);
        
        for (InspectionTask task : unassignedTasks) {
            try {
                log.info("重新分配巡检任务：任务 ID={}, 任务编号={}", task.getId(), task.getTaskNo());
                
                Long newSafetyOfficerId = selectAvailableSafetyOfficer(task);
                if (newSafetyOfficerId != null) {
                    task.setExecutorId(newSafetyOfficerId);
                    task.setStatus(0);
                    task.setUpdateTime(LocalDateTime.now());
                    inspectionTaskMapper.updateById(task);
                    
                    log.info("巡检任务重新分配完成：任务 ID={}, 新安全员 ID={}", task.getId(), newSafetyOfficerId);
                    
                    sendInspectionTaskNotification(newSafetyOfficerId, task.getId(), task.getTaskNo(), "安全员请假重新分配");
                } else {
                    log.warn("巡检任务无可用安全员：任务 ID={}", task.getId());
                }
            } catch (Exception e) {
                log.error("巡检任务重新分配失败：任务 ID={}, 错误={}", task.getId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dynamicAdjustForRouteBlock(Long routeId) {
        log.info("线路堵塞动态调整：线路ID={}", routeId);
        
        LambdaQueryWrapper<TransportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransportTask::getStatus, 0).or().eq(TransportTask::getStatus, 1);
        List<TransportTask> pendingTasks = transportTaskMapper.selectList(wrapper);
        
        for (TransportTask task : pendingTasks) {
            try {
                RouteTemplate newRoute = findAlternativeRoute(task, routeId);
                if (newRoute != null) {
                    task.setStartLocation(newRoute.getStartLocation());
                    task.setStartLongitude(newRoute.getStartLongitude());
                    task.setStartLatitude(newRoute.getStartLatitude());
                    task.setEndLocation(newRoute.getEndLocation());
                    task.setEndLongitude(newRoute.getEndLongitude());
                    task.setEndLatitude(newRoute.getEndLatitude());
                    task.setUpdateTime(LocalDateTime.now());
                    transportTaskMapper.updateById(task);
                    log.info("任务重新规划路线：任务ID={}, 新路线ID={}", task.getId(), newRoute.getId());
                }
            } catch (Exception e) {
                log.error("动态调整失败：任务ID={}, 错误={}", task.getId(), e.getMessage());
            }
        }
    }

    private RouteTemplate findAlternativeRoute(TransportTask task, Long blockedRouteId) {
        List<RouteTemplate> routes = routeTemplateMapper.selectList(
            new LambdaQueryWrapper<RouteTemplate>()
                .eq(RouteTemplate::getStatus, 1)
                .ne(RouteTemplate::getId, blockedRouteId)
                .orderByAsc(RouteTemplate::getDistance)
        );
        
        if (!routes.isEmpty()) {
            return routes.get(0);
        }
        
        return null;
    }
    
    /**
     * 发送任务调整通知
     * 
     * @param userId 用户 ID
     * @param taskId 任务 ID
     * @param taskNo 任务编号
     * @param reason 调整原因
     */
    private void sendTaskNotification(Long userId, Long taskId, String taskNo, String reason) {
        try {
            String title = "调度任务调整通知";
            String content = String.format("您的任务（编号：%s）已%s，请及时查看。", taskNo, reason);
            
            messageClient.sendMessage(
                userId,
                title,
                content,
                "TASK_ADJUSTMENT",
                taskId.toString()
            );
            
            log.info("任务调整通知已发送：用户 ID={}, 任务 ID={}, 原因={}", userId, taskId, reason);
        } catch (Exception e) {
            log.error("发送任务调整通知失败：用户 ID={}, 任务 ID={}, 错误={}", userId, taskId, e.getMessage());
        }
    }
    
    /**
     * 发送维修任务调整通知
     * 
     * @param userId 用户 ID
     * @param taskId 任务 ID
     * @param taskNo 任务编号
     * @param reason 调整原因
     */
    private void sendMaintenanceTaskNotification(Long userId, Long taskId, String taskNo, String reason) {
        try {
            String title = "维修任务调整通知";
            String content = String.format("您的维修任务（编号：%s）已%s，请及时查看。", taskNo, reason);
            
            messageClient.sendMessage(
                userId,
                title,
                content,
                "MAINTENANCE_TASK_ADJUSTMENT",
                taskId.toString()
            );
            
            log.info("维修任务调整通知已发送：用户 ID={}, 任务 ID={}, 原因={}", userId, taskId, reason);
        } catch (Exception e) {
            log.error("发送维修任务调整通知失败：用户 ID={}, 任务 ID={}, 错误={}", userId, taskId, e.getMessage());
        }
    }
    
    /**
     * 发送巡检任务调整通知
     * 
     * @param userId 用户 ID
     * @param taskId 任务 ID
     * @param taskNo 任务编号
     * @param reason 调整原因
     */
    private void sendInspectionTaskNotification(Long userId, Long taskId, String taskNo, String reason) {
        try {
            String title = "巡检任务调整通知";
            String content = String.format("您的巡检任务（编号：%s）已%s，请及时查看。", taskNo, reason);
            
            messageClient.sendMessage(
                userId,
                title,
                content,
                "INSPECTION_TASK_ADJUSTMENT",
                taskId.toString()
            );
            
            log.info("巡检任务调整通知已发送：用户 ID={}, 任务 ID={}, 原因={}", userId, taskId, reason);
        } catch (Exception e) {
            log.error("发送巡检任务调整通知失败：用户 ID={}, 任务 ID={}, 错误={}", userId, taskId, e.getMessage());
        }
    }
    
    /**
     * 选择可用的维修员
     * 
     * @param task 维修任务
     * @return 维修员 ID
     */
    private Long selectAvailableRepairman(MaintenanceTask task) {
        try {
            Result<List<DriverInfo>> result = driverClient.getAvailableRepairmen();
            List<DriverInfo> repairmen = result != null && result.getData() != null ? result.getData() : Collections.emptyList();
            
            if (repairmen.isEmpty()) {
                log.warn("没有可用维修员");
                return null;
            }
            
            List<DriverInfo> sortedRepairmen = repairmen.stream()
                .filter(r -> r.getScore() != null && r.getScore() >= 10)
                .sorted(Comparator.comparing(DriverInfo::getScore).reversed())
                .toList();
            
            if (sortedRepairmen.isEmpty()) {
                log.warn("没有符合条件的维修员");
                return null;
            }
            
            String priority = task.getPriority();
            int totalRepairmen = sortedRepairmen.size();
            int selectionRange = getSelectionRangeByPriority(priority, totalRepairmen);
            
            DriverInfo bestRepairman = sortedRepairmen.stream()
                .limit(selectionRange)
                .findFirst()
                .orElse(sortedRepairmen.getFirst());
            
            log.info("选择最佳维修员：维修员 ID={}, 分数={}, 优先级={}, 候选范围={}/{}", 
                bestRepairman.getId(), bestRepairman.getScore(), priority, selectionRange, totalRepairmen);
            return bestRepairman.getId();
            
        } catch (Exception e) {
            log.error("获取可用维修员失败：{}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 选择可用的安全员
     * 
     * @param task 巡检任务
     * @return 安全员 ID
     */
    private Long selectAvailableSafetyOfficer(InspectionTask task) {
        try {
            Result<List<DriverInfo>> result = driverClient.getAvailableSafetyOfficers();
            List<DriverInfo> safetyOfficers = result != null && result.getData() != null ? result.getData() : Collections.emptyList();
            
            if (safetyOfficers.isEmpty()) {
                log.warn("没有可用安全员");
                return null;
            }
            
            List<DriverInfo> sortedSafetyOfficers = safetyOfficers.stream()
                .filter(s -> s.getScore() != null && s.getScore() >= 10)
                .sorted(Comparator.comparing(DriverInfo::getScore).reversed())
                .toList();
            
            if (sortedSafetyOfficers.isEmpty()) {
                log.warn("没有符合条件的安全员");
                return null;
            }
            
            String priority = task.getPriority();
            int totalSafetyOfficers = sortedSafetyOfficers.size();
            int selectionRange = getSelectionRangeByPriority(priority, totalSafetyOfficers);
            
            DriverInfo bestSafetyOfficer = sortedSafetyOfficers.stream()
                .limit(selectionRange)
                .findFirst()
                .orElse(sortedSafetyOfficers.get(0));
            
            log.info("选择最佳安全员：安全员 ID={}, 分数={}, 优先级={}, 候选范围={}/{}", 
                bestSafetyOfficer.getId(), bestSafetyOfficer.getScore(), priority, selectionRange, totalSafetyOfficers);
            return bestSafetyOfficer.getId();
            
        } catch (Exception e) {
            log.error("获取可用安全员失败：{}", e.getMessage());
        }
        
        return null;
    }

    @Override
    public TransportTask createDispatchTask(TransportTask task) {
        task.setTaskNo(generateTaskNo());
        task.setStatus(0);
        task.setDeleted(0);
        task.setExecutorId(0L);
        task.setVehicleId(0L);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        
        transportTaskMapper.insert(task);
        log.info("创建调度任务：编号={}, 状态={}", task.getTaskNo(), task.getStatus());
        
        return task;
    }

    @Override
    public TransportTask updateDispatchTask(TransportTask task) {
        task.setUpdateTime(LocalDateTime.now());
        transportTaskMapper.updateById(task);
        log.info("更新调度任务：ID={}, 状态={}", task.getId(), task.getStatus());
        return task;
    }

    @Override
    public boolean deleteDispatchTask(Long taskId) {
        log.info("删除调度任务：ID={}", taskId);
        TransportTask task = transportTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("调度任务不存在：ID={}", taskId);
            return false;
        }
        // 使用removeById方法删除任务，这样会设置deleted=1
        boolean result = transportTaskMapper.deleteById(taskId) > 0;
        return result;
    }

    @Override
    public List<TransportTask> getDispatchTasksByPlanId(Long planId) {
        log.info("获取计划下的调度任务：planId={}", planId);
        LambdaQueryWrapper<TransportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransportTask::getPlanId, planId)
               .orderByAsc(TransportTask::getTaskSequence);
        return transportTaskMapper.selectList(wrapper);
    }

    @Override
    public List<TransportTask> getPendingTasksByVehicle(Long vehicleId) {
        log.info("获取车辆待处理任务：vehicleId={}", vehicleId);
        return transportTaskMapper.findPendingByVehicleId(vehicleId);
    }

    @Override
    public List<DispatchTaskVO> getPendingTasksByDriver(Long userId) {
        log.info("获取司机待处理任务：userId={}", userId);
        
        // 直接使用 userId 作为 executor_id 查询，因为 executor_id 存储的是用户ID
        List<TransportTask> tasks = transportTaskMapper.findPendingByExecutorId(userId);
        return tasks.stream()
            .map(this::convertTransportTaskToVO)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignVehicle(Long taskId, Long vehicleId) {
        log.info("分配车辆：taskId={}, vehicleId={}", taskId, vehicleId);
        TransportTask task = transportTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("调度任务不存在：" + taskId);
        }
        task.setVehicleId(vehicleId);
        task.setUpdateTime(LocalDateTime.now());
        transportTaskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignDriver(Long taskId, Long driverId) {
        log.info("分配司机：taskId={}, driverId={}", taskId, driverId);
        TransportTask task = transportTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("调度任务不存在：" + taskId);
        }
        task.setExecutorId(driverId);
        task.setUpdateTime(LocalDateTime.now());
        transportTaskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startTask(Long taskId) {
        log.info("开始任务：taskId={}", taskId);
        TransportTask task = transportTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("调度任务不存在：" + taskId);
        }
        task.setStatus(2);
        task.setActualStartTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        transportTaskMapper.updateById(task);
        
        // 创建行程
        Long driverId = task.getExecutorId();
        Long vehicleId = task.getVehicleId();
        if (driverId != null && vehicleId != null) {
            try {
                return createTripFromTask(task, driverId, vehicleId);
            } catch (Exception e) {
                log.error("创建行程失败：任务 ID={}, 错误={}", taskId, e.getMessage());
            }
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskStatusToInProgress(Long taskId) {
        log.info("更新任务状态为进行中（trip模块回调）：taskId={}", taskId);
        TransportTask task = transportTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("调度任务不存在：" + taskId);
        }
        task.setStatus(2); // 进行中
        task.setActualStartTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        transportTaskMapper.updateById(task);
        log.info("任务状态已更新为进行中：taskId={}, taskNo={}", taskId, task.getTaskNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long taskId) {
        log.info("完成任务：taskId={}", taskId);
        TransportTask task = transportTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("调度任务不存在：" + taskId);
        }
        task.setStatus(3);
        task.setActualEndTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        transportTaskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reassignTask(Long taskId, Long newVehicleId, Long newDriverId) {
        log.info("重新分配任务：taskId={}, newVehicleId={}, newDriverId={}", taskId, newVehicleId, newDriverId);
        TransportTask task = transportTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("调度任务不存在：" + taskId);
        }
        task.setVehicleId(newVehicleId);
        task.setExecutorId(newDriverId);
        task.setUpdateTime(LocalDateTime.now());
        transportTaskMapper.updateById(task);
        
        if (newDriverId != null) {
            sendTaskNotification(newDriverId, taskId, task.getTaskNo(), "任务重新分配");
        }
    }

    @Override
    public List<TransportTask> getAvailableTasksForReassignment(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("获取可重新分配任务：startTime={}, endTime={}", startTime, endTime);
        LambdaQueryWrapper<TransportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(TransportTask::getStatus, 0, 1)
               .ge(TransportTask::getScheduledStartTime, startTime)
               .le(TransportTask::getScheduledEndTime, endTime)
               .orderByAsc(TransportTask::getScheduledStartTime);
        return transportTaskMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoGenerateDispatchTasks() {
        log.info("自动生成调度任务");
        return 0;
    }

    @Override
    public boolean cancelDispatchTask(Long taskId) {
        log.info("取消调度任务：ID={}", taskId);
        
        TransportTask transportTask = transportTaskMapper.selectById(taskId);
        if (transportTask != null) {
            transportTask.setStatus(4);
            transportTask.setUpdateTime(LocalDateTime.now());
            transportTaskMapper.updateById(transportTask);
            log.info("运输任务已取消：ID={}", taskId);
            return true;
        }
        
        MaintenanceTask maintenanceTask = maintenanceTaskMapper.selectById(taskId);
        if (maintenanceTask != null) {
            maintenanceTask.setStatus(4);
            maintenanceTask.setUpdateTime(LocalDateTime.now());
            maintenanceTaskMapper.updateById(maintenanceTask);
            log.info("维修任务已取消：ID={}", taskId);
            return true;
        }
        
        InspectionTask inspectionTask = inspectionTaskMapper.selectById(taskId);
        if (inspectionTask != null) {
            inspectionTask.setStatus(4);
            inspectionTask.setUpdateTime(LocalDateTime.now());
            inspectionTaskMapper.updateById(inspectionTask);
            log.info("巡检任务已取消：ID={}", taskId);
            return true;
        }
        
        log.error("调度任务不存在：ID={}", taskId);
        return false;
    }

    @Override
    public TransportTask getDispatchTask(Long taskId) {
        TransportTask task = transportTaskMapper.selectById(taskId);
        return task != null && (task.getDeleted() == null || task.getDeleted() == 0) ? task : null;
    }

    @Override
    public List<TransportTask> getDispatchTaskList(Integer status, LocalDateTime startDate, LocalDateTime endDate) {
        LambdaQueryWrapper<TransportTask> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(TransportTask::getStatus, status);
        }
        if (startDate != null) {
            wrapper.ge(TransportTask::getScheduledStartTime, startDate);
        }
        if (endDate != null) {
            wrapper.le(TransportTask::getScheduledStartTime, endDate);
        }
        
        wrapper.orderByDesc(TransportTask::getCreateTime);
        
        return transportTaskMapper.selectList(wrapper);
    }

    @Override
    public List<DispatchTaskVO> getAllTaskList(Integer status, LocalDateTime startDate, LocalDateTime endDate) {
        List<DispatchTaskVO> allTasks = new java.util.ArrayList<>();
        
        List<TransportTask> transportTasks = getDispatchTaskList(status, startDate, endDate);
        for (TransportTask task : transportTasks) {
            allTasks.add(convertTransportTaskToVO(task));
        }
        
        LambdaQueryWrapper<MaintenanceTask> maintenanceWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            maintenanceWrapper.eq(MaintenanceTask::getStatus, status);
        }
        if (startDate != null) {
            maintenanceWrapper.ge(MaintenanceTask::getScheduledStartTime, startDate);
        }
        if (endDate != null) {
            maintenanceWrapper.le(MaintenanceTask::getScheduledStartTime, endDate);
        }
        maintenanceWrapper.orderByDesc(MaintenanceTask::getCreateTime);
        List<MaintenanceTask> maintenanceTasks = maintenanceTaskMapper.selectList(maintenanceWrapper);
        for (MaintenanceTask task : maintenanceTasks) {
            allTasks.add(convertMaintenanceTaskToVO(task));
        }
        
        LambdaQueryWrapper<InspectionTask> inspectionWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            inspectionWrapper.eq(InspectionTask::getStatus, status);
        }
        if (startDate != null) {
            inspectionWrapper.ge(InspectionTask::getScheduledStartTime, startDate);
        }
        if (endDate != null) {
            inspectionWrapper.le(InspectionTask::getScheduledStartTime, endDate);
        }
        inspectionWrapper.orderByDesc(InspectionTask::getCreateTime);
        List<InspectionTask> inspectionTasks = inspectionTaskMapper.selectList(inspectionWrapper);
        for (InspectionTask task : inspectionTasks) {
            allTasks.add(convertInspectionTaskToVO(task));
        }
        
        allTasks.sort((a, b) -> {
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });
        
        return allTasks;
    }

    @Override
    public List<DispatchTaskVO> getTasksByPlanId(Long planId) {
        List<DispatchTaskVO> allTasks = new java.util.ArrayList<>();
        
        LambdaQueryWrapper<TransportTask> transportWrapper = new LambdaQueryWrapper<>();
        transportWrapper.eq(TransportTask::getPlanId, planId);
        List<TransportTask> transportTasks = transportTaskMapper.selectList(transportWrapper);
        for (TransportTask task : transportTasks) {
            allTasks.add(convertTransportTaskToVO(task));
        }
        
        LambdaQueryWrapper<MaintenanceTask> maintenanceWrapper = new LambdaQueryWrapper<>();
        maintenanceWrapper.eq(MaintenanceTask::getPlanId, planId);
        List<MaintenanceTask> maintenanceTasks = maintenanceTaskMapper.selectList(maintenanceWrapper);
        for (MaintenanceTask task : maintenanceTasks) {
            allTasks.add(convertMaintenanceTaskToVO(task));
        }
        
        LambdaQueryWrapper<InspectionTask> inspectionWrapper = new LambdaQueryWrapper<>();
        inspectionWrapper.eq(InspectionTask::getPlanId, planId);
        List<InspectionTask> inspectionTasks = inspectionTaskMapper.selectList(inspectionWrapper);
        for (InspectionTask task : inspectionTasks) {
            allTasks.add(convertInspectionTaskToVO(task));
        }
        
        return allTasks;
    }

    private DispatchTaskVO convertTransportTaskToVO(TransportTask task) {
        DispatchTaskVO vo = new DispatchTaskVO();
        vo.setId(task.getId());
        vo.setTaskNo(task.getTaskNo());
        vo.setPlanId(task.getPlanId());
        vo.setTaskType(1);
        vo.setTaskTypeName("运输任务");
        vo.setRouteId(task.getRouteId());
        vo.setTaskSequence(task.getTaskSequence());
        vo.setVehicleId(task.getVehicleId());
        vo.setExecutorId(task.getExecutorId());
        vo.setExecutorName(getExecutorName(task.getExecutorId()));
        vo.setTripId(task.getTripId());
        vo.setStartLocation(task.getStartLocation());
        vo.setStartLongitude(task.getStartLongitude());
        vo.setStartLatitude(task.getStartLatitude());
        vo.setEndLocation(task.getEndLocation());
        vo.setEndLongitude(task.getEndLongitude());
        vo.setEndLatitude(task.getEndLatitude());
        vo.setCargoWeight(task.getCargoWeight());
        vo.setCargoType(task.getCargoType());
        vo.setScheduledStartTime(task.getScheduledStartTime());
        vo.setScheduledEndTime(task.getScheduledEndTime());
        vo.setActualStartTime(task.getActualStartTime());
        vo.setActualEndTime(task.getActualEndTime());
        vo.setStatus(task.getStatus());
        vo.setStatusName(getTaskStatusName(task.getStatus()));
        vo.setPriority(task.getPriority());
        vo.setPushTime(task.getPushTime());
        vo.setAcceptTime(task.getAcceptTime());
        vo.setDescription(task.getDescription());
        vo.setRemark(task.getRemark());
        vo.setCreateTime(task.getCreateTime());
        
        if (task.getVehicleId() != null) {
            try {
                var result = vehicleClient.getById(task.getVehicleId());
                if (result != null && result.getData() != null) {
                    vo.setVehicleNo(result.getData().getVehicleNo());
                }
            } catch (Exception e) {
                log.warn("获取车辆信息失败：vehicleId={}", task.getVehicleId());
            }
        }
        
        return vo;
    }

    private DispatchTaskVO convertMaintenanceTaskToVO(MaintenanceTask task) {
        DispatchTaskVO vo = new DispatchTaskVO();
        vo.setId(task.getId());
        vo.setTaskNo(task.getTaskNo());
        vo.setPlanId(task.getPlanId());
        vo.setTaskType(2);
        vo.setTaskTypeName("维修任务");
        vo.setVehicleId(task.getVehicleId());
        vo.setExecutorId(task.getExecutorId());
        vo.setExecutorName(getExecutorName(task.getExecutorId()));
        vo.setScheduledStartTime(task.getScheduledStartTime());
        vo.setScheduledEndTime(task.getScheduledEndTime());
        vo.setActualStartTime(task.getActualStartTime());
        vo.setActualEndTime(task.getActualEndTime());
        vo.setStatus(task.getStatus());
        vo.setStatusName(getTaskStatusName(task.getStatus()));
        vo.setPriority(task.getPriority());
        vo.setPushTime(task.getPushTime());
        vo.setAcceptTime(task.getAcceptTime());
        vo.setDescription(task.getDescription());
        vo.setRemark(task.getRemark());
        vo.setCreateTime(task.getCreateTime());
        
        if (task.getVehicleId() != null) {
            try {
                var result = vehicleClient.getById(task.getVehicleId());
                if (result != null && result.getData() != null) {
                    vo.setVehicleNo(result.getData().getVehicleNo());
                }
            } catch (Exception e) {
                log.warn("获取车辆信息失败：vehicleId={}", task.getVehicleId());
            }
        }
        
        return vo;
    }

    private DispatchTaskVO convertInspectionTaskToVO(InspectionTask task) {
        DispatchTaskVO vo = new DispatchTaskVO();
        vo.setId(task.getId());
        vo.setTaskNo(task.getTaskNo());
        vo.setPlanId(task.getPlanId());
        vo.setTaskType(3);
        vo.setTaskTypeName("巡检任务");
        vo.setVehicleId(task.getVehicleId());
        vo.setExecutorId(task.getExecutorId());
        vo.setExecutorName(getExecutorName(task.getExecutorId()));
        vo.setScheduledStartTime(task.getScheduledStartTime());
        vo.setScheduledEndTime(task.getScheduledEndTime());
        vo.setActualStartTime(task.getActualStartTime());
        vo.setActualEndTime(task.getActualEndTime());
        vo.setStatus(task.getStatus());
        vo.setStatusName(getTaskStatusName(task.getStatus()));
        vo.setPriority(task.getPriority());
        vo.setPushTime(task.getPushTime());
        vo.setAcceptTime(task.getAcceptTime());
        vo.setDescription(task.getDescription());
        vo.setRemark(task.getRemark());
        vo.setCreateTime(task.getCreateTime());
        
        if (task.getVehicleId() != null) {
            try {
                var result = vehicleClient.getById(task.getVehicleId());
                if (result != null && result.getData() != null) {
                    vo.setVehicleNo(result.getData().getVehicleNo());
                }
            } catch (Exception e) {
                log.warn("获取车辆信息失败：vehicleId={}", task.getVehicleId());
            }
        }
        
        return vo;
    }
    
    private String getExecutorName(Long executorId) {
        if (executorId == null) {
            return null;
        }
        try {
            Result<DriverInfo> driverResult = driverClient.getByUserId(executorId);
            if (driverResult != null && driverResult.getCode() == 200 && driverResult.getData() != null) {
                return driverResult.getData().getDriverName();
            }
        } catch (Exception e) {
            log.warn("获取执行人姓名失败：executorId={}", executorId, e);
        }
        return null;
    }

    private List<Long> getLeaveUserIds() {
        try {
            // 调用 user 服务获取今日请假的用户列表
            Result<List<Long>> result = userClient.getLeaveUserIds();
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("获取请假用户列表失败", e);
        }
        return Collections.emptyList();
    }

    private String getTaskStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待接单";
            case 1 -> "已接单";
            case 2 -> "执行中";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知";
        };
    }

    private String generateTaskNo() {
        return "TRANS" + LocalDateTime.now().format(FORMATTER) + (int)(Math.random() * 1000);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rescheduleTask(Long taskId) {
        log.info("重新调度已取消的任务：任务ID={}", taskId);
        
        try {
            TransportTask transportTask = transportTaskMapper.selectById(taskId);
            if (transportTask != null) {
                return rescheduleTransportTask(transportTask);
            }
            
            MaintenanceTask maintenanceTask = maintenanceTaskMapper.selectById(taskId);
            if (maintenanceTask != null) {
                return rescheduleMaintenanceTask(maintenanceTask);
            }
            
            InspectionTask inspectionTask = inspectionTaskMapper.selectById(taskId);
            if (inspectionTask != null) {
                return rescheduleInspectionTask(inspectionTask);
            }
            
            log.error("任务不存在：ID={}", taskId);
            throw new DispatchException(DispatchResultCode.TASK_NOT_FOUND);
        } catch (DispatchException e) {
            throw e;
        } catch (Exception e) {
            log.error("重新调度任务失败：任务ID={}, 错误={}", taskId, e.getMessage());
            throw new DispatchException(DispatchResultCode.TASK_OPERATION_FAILED, "重新调度任务失败", e);
        }
    }
    
    private boolean rescheduleTransportTask(TransportTask task) {
        log.info("重新调度运输任务：任务ID={}", task.getId());
        
        if (task.getStatus() != 4) {
            log.error("任务状态不是已取消，无法重新调度：任务ID={}, 状态={}", task.getId(), task.getStatus());
            throw new DispatchException(DispatchResultCode.TASK_STATUS_ERROR, "任务状态不是已取消，无法重新调度");
        }
        
        task.setStatus(0);
        task.setExecutorId(null);
        task.setVehicleId(null);
        task.setAcceptTime(null);
        task.setActualStartTime(null);
        task.setActualEndTime(null);
        task.setUpdateTime(LocalDateTime.now());
        
        DriverInfo newDriver = selectBestDriverByPriority(task, null);
        if (newDriver == null) {
            log.error("无可用司机：任务 ID={}", task.getId());
            throw new DispatchException(DispatchResultCode.NO_AVAILABLE_DRIVER, "暂无可用司机，请稍后重试");
        }
        task.setExecutorId(newDriver.getUserId());
        
        Long newVehicleId = selectVehicleByDriverCommonVehicles(newDriver.getUserId(), task);
        if (newVehicleId == null) {
            newVehicleId = selectBestVehicle(task, newDriver.getId());
        }
        if (newVehicleId != null) {
            task.setVehicleId(newVehicleId);
        }
        
        transportTaskMapper.updateById(task);
        log.info("运输任务重新调度完成：任务ID={}, 新司机ID={}, 新用户ID={}, 新车辆ID={}", 
            task.getId(), newDriver.getId(), newDriver.getUserId(), newVehicleId);
        
        sendTaskNotification(newDriver.getUserId(), task.getId(), task.getTaskNo(), "任务重新调度");
        
        return true;
    }
    
    private boolean rescheduleMaintenanceTask(MaintenanceTask task) {
        log.info("重新调度维修任务：任务ID={}", task.getId());
        
        if (task.getStatus() != 4) {
            log.error("任务状态不是已取消，无法重新调度：任务ID={}, 状态={}", task.getId(), task.getStatus());
            throw new DispatchException(DispatchResultCode.TASK_STATUS_ERROR, "任务状态不是已取消，无法重新调度");
        }
        
        task.setStatus(0);
        task.setExecutorId(null);
        task.setUpdateTime(LocalDateTime.now());
        
        Long newRepairmanId = selectAvailableRepairman(task);
        if (newRepairmanId == null) {
            log.error("无可用维修员：任务 ID={}", task.getId());
            throw new DispatchException(DispatchResultCode.NO_AVAILABLE_REPAIRMAN, "暂无可用维修员，请稍后重试");
        }
        task.setExecutorId(newRepairmanId);
        
        if (task.getVehicleId() == null) {
            Long faultVehicleId = selectFaultVehicle();
            if (faultVehicleId != null) {
                task.setVehicleId(faultVehicleId);
            }
        }
        
        maintenanceTaskMapper.updateById(task);
        log.info("维修任务重新调度完成：任务ID={}, 新维修员ID={}", task.getId(), newRepairmanId);
        
        sendMaintenanceTaskNotification(newRepairmanId, task.getId(), task.getTaskNo(), "任务重新调度");
        
        return true;
    }
    
    private boolean rescheduleInspectionTask(InspectionTask task) {
        log.info("重新调度巡检任务：任务ID={}", task.getId());
        
        if (task.getStatus() != 4) {
            log.error("任务状态不是已取消，无法重新调度：任务ID={}, 状态={}", task.getId(), task.getStatus());
            throw new DispatchException(DispatchResultCode.TASK_STATUS_ERROR, "任务状态不是已取消，无法重新调度");
        }
        
        task.setStatus(0);
        task.setExecutorId(null);
        task.setVehicleId(null);
        task.setUpdateTime(LocalDateTime.now());
        
        Long newSafetyOfficerId = selectAvailableSafetyOfficer(task);
        if (newSafetyOfficerId == null) {
            log.error("无可用安全员：任务 ID={}", task.getId());
            throw new DispatchException(DispatchResultCode.NO_AVAILABLE_SAFETY_OFFICER, "暂无可用安全员，请稍后重试");
        }
        task.setExecutorId(newSafetyOfficerId);
        
        Long newVehicleId = selectAvailableVehicle();
        if (newVehicleId != null) {
            task.setVehicleId(newVehicleId);
        }
        
        inspectionTaskMapper.updateById(task);
        log.info("巡检任务重新调度完成：任务ID={}, 新安全员ID={}, 新车辆ID={}", task.getId(), newSafetyOfficerId, newVehicleId);
        
        sendInspectionTaskNotification(newSafetyOfficerId, task.getId(), task.getTaskNo(), "任务重新调度");
        
        return true;
    }
    
    private Long selectFaultVehicle() {
        try {
            var result = vehicleClient.getFaultVehicles();
            List<VehicleInfo> vehicles = result != null ? result.getData() : null;
            if (vehicles != null && !vehicles.isEmpty()) {
                return vehicles.get(0).getId();
            }
        } catch (Exception e) {
            log.error("获取故障车辆失败", e);
        }
        return null;
    }
    
    private Long selectAvailableVehicle() {
        try {
            var result = vehicleClient.getAvailableVehicles();
            List<VehicleInfo> vehicles = result != null ? result.getData() : null;
            if (vehicles != null && !vehicles.isEmpty()) {
                return vehicles.get(0).getId();
            }
        } catch (Exception e) {
            log.error("获取可用车辆失败", e);
        }
        return null;
    }

    @Override
    public Long createMaintenanceTaskFromFault(java.util.Map<String, Object> faultInfo) {
        log.info("从故障申报创建维修任务: {}", faultInfo);
        
        Long vehicleId = faultInfo.get("vehicleId") != null ? Long.valueOf(faultInfo.get("vehicleId").toString()) : null;
        String faultType = faultInfo.get("faultType") != null ? faultInfo.get("faultType").toString() : null;
        String faultDescription = faultInfo.get("faultDescription") != null ? faultInfo.get("faultDescription").toString() : null;
        Integer severity = faultInfo.get("severity") != null ? Integer.valueOf(faultInfo.get("severity").toString()) : 1;
        Long faultId = faultInfo.get("faultId") != null ? Long.valueOf(faultInfo.get("faultId").toString()) : null;
        BigDecimal latitude = faultInfo.get("latitude") != null ? new BigDecimal(faultInfo.get("latitude").toString()) : null;
        BigDecimal longitude = faultInfo.get("longitude") != null ? new BigDecimal(faultInfo.get("longitude").toString()) : null;
        String locationAddress = faultInfo.get("locationAddress") != null ? faultInfo.get("locationAddress").toString() : null;
        
        Long repairmanId;
        if (severity != null && severity >= 3) {
            repairmanId = selectUrgentRepairman();
            log.info("严重故障(severity={})，使用紧急维修员选择策略", severity);
        } else {
            repairmanId = selectAvailableRepairman();
        }
        if (repairmanId == null) {
            log.warn("没有可用的维修员，无法创建维修任务");
            return null;
        }
        
        MaintenanceTask task = new MaintenanceTask();
        task.setTaskNo(generateMaintenanceTaskNo());
        task.setVehicleId(vehicleId);
        task.setExecutorId(repairmanId);
        task.setFaultType(parseFaultType(faultType));
        task.setFaultLevel(severity);
        task.setFaultDescription(faultDescription);
        task.setFaultLocation(locationAddress);
        if (latitude != null) {
            task.setFaultLatitude(latitude.doubleValue());
        }
        if (longitude != null) {
            task.setFaultLongitude(longitude.doubleValue());
        }
        task.setStatus(0);
        task.setPriority(severity != null && severity >= 3 ? "urgent" : "normal");
        
        LocalDateTime scheduledStart = LocalDateTime.now();
        task.setScheduledStartTime(scheduledStart);
        task.setScheduledEndTime(scheduledStart.plusHours(4));
        
        maintenanceTaskMapper.insert(task);
        log.info("创建维修任务成功： taskId={}, taskNo={}, executorId={}", 
            task.getId(), task.getTaskNo(), repairmanId);
        
        return task.getId();
    }
    
    private Long selectAvailableRepairman() {
        try {
            Result<List<DriverInfo>> result = driverClient.getAvailableRepairmen();
            List<DriverInfo> repairmen = result != null && result.getData() != null ? result.getData() : Collections.emptyList();
            
            if (repairmen.isEmpty()) {
                log.warn("没有可用维修员");
                return null;
            }
            
            return repairmen.get(0).getId();
        } catch (Exception e) {
            log.error("获取可用维修员失败: {}", e.getMessage());
        }
        return null;
    }

    private Long selectUrgentRepairman() {
        try {
            Result<List<DriverInfo>> result = driverClient.getAvailableRepairmen();
            List<DriverInfo> repairmen = result != null && result.getData() != null ? result.getData() : Collections.emptyList();

            if (repairmen.isEmpty()) {
                log.warn("没有可用维修员（紧急模式）");
                return null;
            }

            List<Long> activeExecutorIds = maintenanceTaskMapper.findActiveExecutorIds();
            log.info("当前有活跃任务的维修员ID: {}", activeExecutorIds);

            for (DriverInfo repairman : repairmen) {
                if (!activeExecutorIds.contains(repairman.getId())) {
                    log.info("紧急故障优先选择空闲维修员：维修员ID={}, 姓名={}", repairman.getId(), repairman.getDriverName());
                    return repairman.getId();
                }
            }

            log.info("所有维修员都有活跃任务，退回选择评分最高的维修员");
            DriverInfo best = repairmen.stream()
                .filter(r -> r.getScore() != null)
                .max(Comparator.comparing(DriverInfo::getScore))
                .orElse(repairmen.get(0));
            return best.getId();
        } catch (Exception e) {
            log.error("获取紧急维修员失败: {}", e.getMessage());
        }
        return null;
    }
    
    private String generateMaintenanceTaskNo() {
        return "MT" + LocalDateTime.now().format(FORMATTER);
    }
    
    private Integer parseFaultType(String faultType) {
        if (faultType == null) return null;
        return switch (faultType) {
            case "发动机" -> 1;
            case "变速箱" -> 2;
            case "制动系统" -> 3;
            case "轮胎" -> 4;
            case "电气系统" -> 5;
            default -> 0;
        };
    }
}
