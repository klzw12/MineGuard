package com.klzw.service.dispatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.core.client.DriverClient;
import com.klzw.common.core.client.MessageClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.client.TripClient;
import com.klzw.common.core.domain.dto.DriverInfo;
import com.klzw.common.core.domain.dto.DriverVehicleInfo;
import com.klzw.common.core.domain.dto.VehicleInfo;
import com.klzw.common.core.domain.dto.TripCreateRequest;
import com.klzw.service.dispatch.entity.TransportTask;
import com.klzw.service.dispatch.entity.RouteTemplate;
import com.klzw.service.dispatch.entity.MaintenanceTask;
import com.klzw.service.dispatch.entity.InspectionTask;
import com.klzw.service.dispatch.mapper.TransportTaskMapper;
import com.klzw.service.dispatch.mapper.RouteTemplateMapper;
import com.klzw.service.dispatch.mapper.MaintenanceTaskMapper;
import com.klzw.service.dispatch.mapper.InspectionTaskMapper;
import com.klzw.service.dispatch.service.DispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
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
            return false;
        }

        Long bestDriverId = selectBestDriverByPriority(task);
        if (bestDriverId == null) {
            log.error("无可用司机：任务 ID={}", taskId);
            return false;
        }
        
        Long bestVehicleId = selectVehicleByDriverCommonVehicles(bestDriverId, task);
        if (bestVehicleId == null) {
            bestVehicleId = selectBestVehicle(task);
            if (bestVehicleId == null) {
                log.error("无可用车辆：任务 ID={}", taskId);
                return false;
            }
        }
        
        task.setExecutorId(bestDriverId);
        task.setVehicleId(bestVehicleId);
        task.setStatus(2);
        task.setActualStartTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        transportTaskMapper.updateById(task);
        
        log.info("智能调度完成：任务 ID={}, 司机 ID={}, 车辆 ID={}", taskId, bestDriverId, bestVehicleId);
        
        // 创建行程
        try {
            createTripFromTask(task, bestDriverId, bestVehicleId);
        } catch (Exception e) {
            log.error("创建行程失败：任务 ID={}, 错误={}", taskId, e.getMessage());
        }
        
        return true;
    }
    
    /**
     * 根据调度任务创建行程
     */
    private void createTripFromTask(TransportTask task, Long driverId, Long vehicleId) {
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
            
            Long tripId = tripClient.createTrip(request).block();
            
            if (tripId != null) {
                log.info("行程创建成功：任务 ID={}, 行程 ID={}", task.getId(), tripId);
            } else {
                log.warn("行程创建返回 null：任务 ID={}", task.getId());
            }
        } catch (Exception e) {
            log.error("调用 TripClient 创建行程失败：{}", e.getMessage());
            throw e;
        }
    }

    private Long selectBestDriverByPriority(TransportTask task) {
        String priority = task.getPriority();
        
        try {
            List<DriverInfo> drivers = driverClient.getAvailableDrivers();
            
            if (drivers == null || drivers.isEmpty()) {
                log.warn("没有可用司机");
                return null;
            }
            
            List<DriverInfo> sortedDrivers = drivers.stream()
                .filter(d -> d.getScore() != null && d.getScore() >= 10)
                .sorted(Comparator.comparing(DriverInfo::getScore).reversed())
                .collect(Collectors.toList());
            
            if (sortedDrivers.isEmpty()) {
                log.warn("没有符合条件的司机");
                return null;
            }
            
            int totalDrivers = sortedDrivers.size();
            int selectionRange = getSelectionRangeByPriority(priority, totalDrivers);
            
            List<DriverInfo> candidateDrivers = sortedDrivers.stream()
                .limit(selectionRange)
                .collect(Collectors.toList());
            
            if (task.getVehicleId() != null) {
                for (DriverInfo driver : candidateDrivers) {
                    if (hasCommonVehicle(driver.getId(), task.getVehicleId())) {
                        log.info("选择常用车辆匹配的司机：司机ID={}, 分数={}, 优先级={}", 
                            driver.getId(), driver.getScore(), priority);
                        return driver.getId();
                    }
                }
            }
            
            DriverInfo bestDriver = candidateDrivers.get(0);
            log.info("选择最佳司机：司机ID={}, 分数={}, 优先级={}, 候选范围={}/{}", 
                bestDriver.getId(), bestDriver.getScore(), priority, selectionRange, totalDrivers);
            return bestDriver.getId();
            
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
            List<DriverVehicleInfo> commonVehicles = driverClient.getCommonVehicles(driverId);
            
            if (commonVehicles != null && !commonVehicles.isEmpty()) {
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
            List<DriverVehicleInfo> vehicles = driverClient.getCommonVehicles(driverId);
            if (vehicles != null) {
                return vehicles.stream()
                    .anyMatch(v -> vehicleId.equals(v.getVehicleId()));
            }
        } catch (Exception e) {
            log.warn("检查常用车辆失败：{}", e.getMessage());
        }
        return false;
    }

    private boolean isVehicleAvailable(Long vehicleId) {
        try {
            VehicleInfo vehicle = vehicleClient.getById(vehicleId);
            return vehicle != null && vehicle.getStatus() != null && vehicle.getStatus() == 0;
        } catch (Exception e) {
            log.warn("检查车辆状态失败：车辆ID={}", vehicleId);
        }
        return false;
    }

    private Long selectBestVehicle(TransportTask task) {
        try {
            VehicleInfo vehicle = vehicleClient.selectBestVehicle(
                task.getStartLongitude(),
                task.getStartLatitude(),
                task.getCargoWeight(),
                task.getScheduledStartTime() != null ? task.getScheduledStartTime().toString() : null
            );
            
            if (vehicle != null && vehicle.getId() != null) {
                return vehicle.getId();
            }
        } catch (Exception e) {
            log.warn("调用车辆服务获取最佳车辆失败：{}", e.getMessage());
        }
        
        return 1L;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dynamicAdjustForVehicleFault(Long vehicleId) {
        log.info("车辆故障动态调整：车辆ID={}", vehicleId);
        
        List<TransportTask> pendingTasks = transportTaskMapper.findPendingByVehicleId(vehicleId);
        
        for (TransportTask task : pendingTasks) {
            try {
                Long newVehicleId = selectBestVehicle(task);
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
                Long newDriverId = selectBestDriverByPriority(task);
                if (newDriverId != null) {
                    task.setExecutorId(newDriverId);
                    task.setUpdateTime(LocalDateTime.now());
                    transportTaskMapper.updateById(task);
                    log.info("任务重新分配司机：任务 ID={}, 新司机 ID={}", task.getId(), newDriverId);
                    
                    // 推送通知给新司机
                    sendTaskNotification(newDriverId, task.getId(), task.getTaskNo(), "司机请假调整");
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
            if ("ROLE_DRIVER".equals(roleCode)) {
                dynamicAdjustForDriverLeave(userId);
            } else if ("ROLE_REPAIRMAN".equals(roleCode)) {
                dynamicAdjustForRepairmanLeave(userId);
            } else if ("ROLE_SAFETY_OFFICER".equals(roleCode)) {
                dynamicAdjustForSafetyOfficerLeave(userId);
            } else {
                log.warn("未知角色编码：{}", roleCode);
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
                
                Long newDriverId = selectBestDriverByPriority(task);
                if (newDriverId != null) {
                    task.setExecutorId(newDriverId);
                    
                    Long newVehicleId = selectVehicleByDriverCommonVehicles(newDriverId, task);
                    if (newVehicleId == null) {
                        newVehicleId = selectBestVehicle(task);
                    }
                    task.setVehicleId(newVehicleId);
                    
                    task.setStatus(0);
                    task.setUpdateTime(LocalDateTime.now());
                    transportTaskMapper.updateById(task);
                    
                    log.info("运输任务重新分配完成：任务 ID={}, 新司机 ID={}, 新车辆 ID={}", task.getId(), newDriverId, newVehicleId);
                    
                    sendTaskNotification(newDriverId, task.getId(), task.getTaskNo(), "司机请假重新分配");
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
            List<DriverInfo> repairmen = driverClient.getAvailableRepairmen();
            
            if (repairmen == null || repairmen.isEmpty()) {
                log.warn("没有可用维修员");
                return null;
            }
            
            List<DriverInfo> sortedRepairmen = repairmen.stream()
                .filter(r -> r.getScore() != null && r.getScore() >= 10)
                .sorted(Comparator.comparing(DriverInfo::getScore).reversed())
                .collect(Collectors.toList());
            
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
                .orElse(sortedRepairmen.get(0));
            
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
            List<DriverInfo> safetyOfficers = driverClient.getAvailableSafetyOfficers();
            
            if (safetyOfficers == null || safetyOfficers.isEmpty()) {
                log.warn("没有可用安全员");
                return null;
            }
            
            List<DriverInfo> sortedSafetyOfficers = safetyOfficers.stream()
                .filter(s -> s.getScore() != null && s.getScore() >= 10)
                .sorted(Comparator.comparing(DriverInfo::getScore).reversed())
                .collect(Collectors.toList());
            
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
    public boolean cancelDispatchTask(Long taskId) {
        log.info("取消调度任务：ID={}", taskId);
        
        TransportTask task = transportTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("调度任务不存在：ID={}", taskId);
            return false;
        }
        
        task.setStatus(4);
        task.setUpdateTime(LocalDateTime.now());
        transportTaskMapper.updateById(task);
        
        return true;
    }

    @Override
    public TransportTask getDispatchTask(Long taskId) {
        return transportTaskMapper.selectById(taskId);
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

    private String generateTaskNo() {
        return "TRANS" + LocalDateTime.now().format(FORMATTER);
    }
}
