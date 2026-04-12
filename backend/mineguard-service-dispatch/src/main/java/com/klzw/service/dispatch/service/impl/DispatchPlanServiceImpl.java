package com.klzw.service.dispatch.service.impl;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.client.DriverClient;
import com.klzw.common.core.client.UserClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.domain.dto.DriverInfo;
import com.klzw.common.core.domain.dto.DriverVehicleInfo;
import com.klzw.common.core.domain.dto.VehicleInfo;
import com.klzw.common.core.result.PageResult;
import com.klzw.common.core.result.Result;
import com.klzw.service.dispatch.dto.DispatchPlanDTO;
import com.klzw.service.dispatch.entity.DispatchPlan;
import com.klzw.service.dispatch.entity.RouteTemplate;
import com.klzw.service.dispatch.entity.TransportTask;
import com.klzw.service.dispatch.entity.MaintenanceTask;
import com.klzw.service.dispatch.entity.InspectionTask;
import com.klzw.service.dispatch.mapper.DispatchPlanMapper;
import com.klzw.service.dispatch.mapper.RouteTemplateMapper;
import com.klzw.service.dispatch.mapper.TransportTaskMapper;
import com.klzw.service.dispatch.mapper.MaintenanceTaskMapper;
import com.klzw.service.dispatch.mapper.InspectionTaskMapper;
import com.klzw.service.dispatch.service.DispatchPlanService;
import com.klzw.service.dispatch.vo.DispatchPlanVO;
import com.klzw.common.mq.producer.IMessageProducer;
import com.klzw.common.mq.constant.MqConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchPlanServiceImpl implements DispatchPlanService {

    private final DispatchPlanMapper dispatchPlanMapper;
    private final RouteTemplateMapper routeTemplateMapper;
    private final DriverClient driverClient;
    private final UserClient userClient;
    private final VehicleClient vehicleClient;
    private final TransportTaskMapper transportTaskMapper;
    private final MaintenanceTaskMapper maintenanceTaskMapper;
    private final InspectionTaskMapper inspectionTaskMapper;
    private final IMessageProducer messageProducer;

    @Override
    public List<DispatchPlanVO> list(Integer status, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<DispatchPlan> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(DispatchPlan::getStatus, status);
        }
        if (startDate != null) {
            wrapper.ge(DispatchPlan::getPlanDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(DispatchPlan::getPlanDate, endDate);
        }
        
        wrapper.orderByDesc(DispatchPlan::getCreateTime);
        
        List<DispatchPlan> plans = dispatchPlanMapper.selectList(wrapper);
        return plans.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<DispatchPlanVO> page(PageRequest pageRequest) {
        Page<DispatchPlan> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        LambdaQueryWrapper<DispatchPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DispatchPlan::getCreateTime);
        
        Page<DispatchPlan> result = dispatchPlanMapper.selectPage(page, wrapper);
        
        List<DispatchPlanVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageResult.of(result.getTotal(), pageRequest.getPage(), pageRequest.getSize(), voList);
    }

    @Override
    public DispatchPlanVO getById(Long id) {
        DispatchPlan plan = dispatchPlanMapper.selectById(id);
        if (plan == null) {
            throw new RuntimeException("调度计划不存在");
        }
        return convertToVO(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DispatchPlanDTO dto) {
        DispatchPlan plan = new DispatchPlan();
        BeanUtils.copyProperties(dto, plan);
        plan.setPlanNo(generatePlanNo());
        plan.setStatus(0);
        plan.setCompletedTrips(0);
        
        if (dto.getRouteId() != null) {
            RouteTemplate route = routeTemplateMapper.selectById(dto.getRouteId());
            if (route != null) {
                plan.setStartLocation(route.getStartLocation());
                plan.setEndLocation(route.getEndLocation());
                plan.setStartLongitude(route.getStartLongitude());
                plan.setStartLatitude(route.getStartLatitude());
                plan.setEndLongitude(route.getEndLongitude());
                plan.setEndLatitude(route.getEndLatitude());
            }
        }
        
        dispatchPlanMapper.insert(plan);
        log.info("创建调度计划成功，计划ID：{}，计划编号：{}，类型：{}", plan.getId(), plan.getPlanNo(), plan.getPlanType());
        
        return plan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, DispatchPlanDTO dto) {
        DispatchPlan plan = dispatchPlanMapper.selectById(id);
        if (plan == null) {
            throw new RuntimeException("调度计划不存在");
        }
        
        if (plan.getStatus() != 0) {
            throw new RuntimeException("只有待执行的计划可以修改");
        }
        
        BeanUtils.copyProperties(dto, plan);
        plan.setId(id);
        
        dispatchPlanMapper.updateById(plan);
        log.info("更新调度计划成功，计划ID：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DispatchPlan plan = dispatchPlanMapper.selectById(id);
        if (plan == null) {
            throw new RuntimeException("调度计划不存在");
        }
        
        if (plan.getStatus() == 1) {
            throw new RuntimeException("执行中的计划不能删除");
        }
        
        dispatchPlanMapper.deleteById(id);
        log.info("删除调度计划成功，计划ID：{}", id);
    }

    @Override
    public List<DispatchPlanVO> getByDate(LocalDate date) {
        LambdaQueryWrapper<DispatchPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DispatchPlan::getPlanDate, date)
               .orderByAsc(DispatchPlan::getStartTimeSlot);
        
        List<DispatchPlan> plans = dispatchPlanMapper.selectList(wrapper);
        return plans.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(Long id) {
        DispatchPlan plan = dispatchPlanMapper.selectById(id);
        if (plan == null) {
            throw new RuntimeException("调度计划不存在");
        }
        
        if (plan.getStatus() != 0) {
            throw new RuntimeException("只有待执行的计划可以执行");
        }
        
        plan.setStatus(1);
        dispatchPlanMapper.updateById(plan);
        
        boolean created = createTasksForPlan(plan);
        
        if (!created) {
            plan.setStatus(0);
            dispatchPlanMapper.updateById(plan);
            throw new RuntimeException("无法分配执行人员或车辆，任务创建失败");
        }
        
        log.info("执行调度计划成功，计划ID：{}，类型：{}，已创建任务", id, plan.getPlanType());
    }
    
    private boolean createTasksForPlan(DispatchPlan plan) {
        Integer planType = plan.getPlanType();
        if (planType == null) {
            planType = 1;
        }
        
        String priority = calculatePriority(plan);
        int taskCount = plan.getPlannedTrips() != null ? plan.getPlannedTrips() : 1;
        
        List<Long> executorIds = new ArrayList<>();
        List<Long> vehicleIds = new ArrayList<>();
        
        if (planType == 1) {
            List<DriverInfo> availableDrivers = getAvailableDriversList();
            List<VehicleInfo> availableVehicles = getAvailableVehiclesList();
            
            if (availableDrivers.size() < taskCount) {
                log.warn("可用司机数量不足: 需要 {}, 实际 {}", taskCount, availableDrivers.size());
                return false;
            }
            if (availableVehicles.size() < taskCount) {
                log.warn("可用车辆数量不足: 需要 {}, 实际 {}", taskCount, availableVehicles.size());
                return false;
            }
            
            Set<Long> assignedVehicleIds = new HashSet<>();
            Long[] driverVehicleAssignments = new Long[taskCount];
            
            for (int i = 0; i < taskCount && i < availableDrivers.size(); i++) {
                DriverInfo driver = availableDrivers.get(i);
                Long commonVehicleId = getAvailableCommonVehicle(driver.getUserId(), availableVehicles, assignedVehicleIds);
                if (commonVehicleId != null) {
                    driverVehicleAssignments[i] = commonVehicleId;
                    assignedVehicleIds.add(commonVehicleId);
                    log.info("第一轮分配: 司机 {} 分配常用车辆 {}", driver.getDriverName(), commonVehicleId);
                }
            }
            
            for (int i = 0; i < taskCount && i < availableDrivers.size(); i++) {
                DriverInfo driver = availableDrivers.get(i);
                executorIds.add(driver.getUserId());
                
                if (driverVehicleAssignments[i] == null) {
                    Long randomVehicleId = assignRandomVehicle(availableVehicles, assignedVehicleIds);
                    if (randomVehicleId != null) {
                        driverVehicleAssignments[i] = randomVehicleId;
                        assignedVehicleIds.add(randomVehicleId);
                        log.info("第二轮分配: 司机 {} 随机分配车辆 {}", driver.getDriverName(), randomVehicleId);
                    } else {
                        log.warn("无法为司机 {} 分配车辆", driver.getDriverName());
                        return false;
                    }
                }
                vehicleIds.add(driverVehicleAssignments[i]);
            }
        } else {
            for (int i = 0; i < taskCount; i++) {
                Long executorId = assignExecutor(planType, null, plan);
                Long vehicleId = assignVehicleForPlan(plan, planType);
                
                if (executorId == null) {
                    log.warn("无法分配执行人员, planId: {}, taskIndex: {}", plan.getId(), i);
                    return false;
                }
                if (vehicleId == null) {
                    log.warn("无法分配车辆, planId: {}, taskIndex: {}", plan.getId(), i);
                    return false;
                }
                
                executorIds.add(executorId);
                vehicleIds.add(vehicleId);
            }
        }
        
        for (int i = 0; i < taskCount; i++) {
            int sequence = i + 1;
            String taskNo = generateTaskNo(plan.getPlanType(), sequence);
            Long executorId = executorIds.get(i);
            Long vehicleId = vehicleIds.get(i);
            
            switch (planType) {
                case 1:
                    createTransportTask(plan, sequence, taskNo, executorId, vehicleId, priority);
                    break;
                case 2:
                    Long repairmanVehicleId = assignRepairmanVehicle();
                    createMaintenanceTask(plan, sequence, taskNo, executorId, vehicleId, repairmanVehicleId, priority);
                    break;
                case 3:
                    createInspectionTask(plan, sequence, taskNo, executorId, vehicleId, priority);
                    break;
                default:
                    log.warn("未知的计划类型: {}", planType);
            }
        }
        
        return true;
    }
    
    private Long getAvailableCommonVehicle(Long driverId, List<VehicleInfo> availableVehicles, Set<Long> assignedVehicleIds) {
        try {
            Result<List<DriverVehicleInfo>> result = driverClient.getCommonVehicles(driverId);
            if (result != null && result.getData() != null && !result.getData().isEmpty()) {
                for (DriverVehicleInfo commonVehicle : result.getData()) {
                    Long vehicleId = commonVehicle.getVehicleId();
                    
                    if (vehicleId != null && !assignedVehicleIds.contains(vehicleId)) {
                        boolean isAvailable = availableVehicles.stream()
                            .anyMatch(v -> v.getId().equals(vehicleId));
                        if (isAvailable) {
                            return vehicleId;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取司机常用车辆失败: driverId={}, error={}", driverId, e.getMessage());
        }
        return null;
    }
    
    private Long assignRandomVehicle(List<VehicleInfo> availableVehicles, Set<Long> assignedVehicleIds) {
        for (VehicleInfo vehicle : availableVehicles) {
            if (!assignedVehicleIds.contains(vehicle.getId())) {
                return vehicle.getId();
            }
        }
        return null;
    }
    
    private List<DriverInfo> getAvailableDriversList() {
        try {
            Result<List<DriverInfo>> result = driverClient.getAvailableDrivers();
            if (result != null && result.getData() != null) {
                List<DriverInfo> drivers = result.getData();
                
                List<Long> assignedDriverIds = transportTaskMapper.findAssignedButNotAcceptedDriverIds();
                log.info("已分配待接单的司机ID列表: {}", assignedDriverIds);
                
                if (!assignedDriverIds.isEmpty()) {
                    List<DriverInfo> filteredDrivers = drivers.stream()
                        .filter(d -> !assignedDriverIds.contains(d.getUserId()))
                        .collect(Collectors.toList());
                    
                    if (filteredDrivers.isEmpty()) {
                        log.info("排除已分配待接单后无可用司机，放宽条件从所有在职司机中选择");
                        return drivers;
                    }
                    return filteredDrivers;
                }
                
                return drivers;
            }
        } catch (Exception e) {
            log.error("获取可用司机列表失败", e);
        }
        return new ArrayList<>();
    }
    
    private List<VehicleInfo> getAvailableVehiclesList() {
        try {
            var result = vehicleClient.getAvailableVehicles();
            List<VehicleInfo> vehicles = result != null ? result.getData() : null;
            if (vehicles != null) {
                List<Long> assignedVehicleIds = transportTaskMapper.findAssignedButNotAcceptedVehicleIds();
                log.info("已分配待接单的车辆ID列表: {}", assignedVehicleIds);
                
                if (!assignedVehicleIds.isEmpty()) {
                    List<VehicleInfo> filteredVehicles = vehicles.stream()
                        .filter(v -> !assignedVehicleIds.contains(v.getId()))
                        .collect(Collectors.toList());
                    
                    if (filteredVehicles.isEmpty()) {
                        log.info("排除已分配待接单后无可用车辆，放宽条件从所有空闲车辆中选择");
                        return vehicles;
                    }
                    return filteredVehicles;
                }
                
                return vehicles;
            }
        } catch (Exception e) {
            log.error("获取可用车辆列表失败", e);
        }
        return new ArrayList<>();
    }
    
    private String calculatePriority(DispatchPlan plan) {
        String endTimeSlot = plan.getEndTimeSlot();
        if (endTimeSlot == null || endTimeSlot.isEmpty()) {
            LocalDate planDate = plan.getPlanDate();
            if (planDate == null) {
                return "normal";
            }
            endTimeSlot = planDate.toString();
        }
        
        try {
            LocalDate deadline = LocalDate.parse(endTimeSlot);
            LocalDate today = LocalDate.now();
            long daysUntilDeadline = java.time.temporal.ChronoUnit.DAYS.between(today, deadline);
            
            if (daysUntilDeadline <= 1) {
                return "urgent";
            } else if (daysUntilDeadline <= 3) {
                return "high";
            } else if (daysUntilDeadline <= 7) {
                return "normal";
            } else {
                return "low";
            }
        } catch (Exception e) {
            log.warn("解析截止日期失败: {}", endTimeSlot, e);
            return "normal";
        }
    }
    
    private Long assignExecutor(Integer planType, Long vehicleId, DispatchPlan plan) {
        try {
            switch (planType) {
                case 1:
                    return assignBestDriver(vehicleId, plan);
                case 2:
                    return assignRandomRepairman();
                case 3:
                    return assignRandomSafetyOfficer();
                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("分配执行人员失败, planType: {}", planType, e);
            return null;
        }
    }
    
    private Long assignBestDriver(Long vehicleId, DispatchPlan plan) {
        try {
            String scheduledTime = plan.getPlanDate() != null ? plan.getPlanDate().toString() : null;
            Result<DriverInfo> result = driverClient.selectBestDriver(vehicleId, scheduledTime);
            
            if (result != null && result.getData() != null) {
                DriverInfo driver = result.getData();
                Long executorId = driver.getUserId();
                log.info("智能分配司机成功, userId: {}, driverName: {}", executorId, driver.getDriverName());
                return executorId;
            }
            
            log.warn("智能分配司机失败，尝试随机选择");
            return assignRandomDriver();
        } catch (Exception e) {
            log.error("智能分配司机失败", e);
            return assignRandomDriver();
        }
    }
    
    private Long assignRandomDriver() {
        try {
            Result<List<DriverInfo>> result = driverClient.getAvailableDrivers();
            if (result != null && result.getData() != null && !result.getData().isEmpty()) {
                List<DriverInfo> drivers = result.getData();
                int randomIndex = (int) (Math.random() * drivers.size());
                DriverInfo driver = drivers.get(randomIndex);
                Long executorId = driver.getUserId();
                log.info("随机分配司机成功, userId: {}, driverName: {}", executorId, driver.getDriverName());
                return executorId;
            }
        } catch (Exception e) {
            log.error("随机选择司机失败", e);
        }
        return null;
    }
    
    private Long assignRandomRepairman() {
        try {
            Result<List<DriverInfo>> result = driverClient.getAvailableRepairmen();
            if (result != null && result.getData() != null && !result.getData().isEmpty()) {
                List<DriverInfo> repairmen = result.getData();
                int randomIndex = (int) (Math.random() * repairmen.size());
                DriverInfo repairman = repairmen.get(randomIndex);
                Long executorId = repairman.getUserId();
                log.info("随机分配维修员成功, userId: {}, driverName: {}", executorId, repairman.getDriverName());
                return executorId;
            }
        } catch (Exception e) {
            log.error("随机选择维修员失败", e);
        }
        return null;
    }
    
    private Long assignRandomSafetyOfficer() {
        try {
            Result<List<DriverInfo>> result = driverClient.getAvailableSafetyOfficers();
            if (result != null && result.getData() != null && !result.getData().isEmpty()) {
                List<DriverInfo> officers = result.getData();
                int randomIndex = (int) (Math.random() * officers.size());
                DriverInfo officer = officers.get(randomIndex);
                Long executorId = officer.getUserId();
                log.info("随机分配安全员成功, userId: {}, driverName: {}", executorId, officer.getDriverName());
                return executorId;
            }
        } catch (Exception e) {
            log.error("随机选择安全员失败", e);
        }
        return null;
    }
    
    private Long assignVehicle(DispatchPlan plan) {
        try {
            var result = vehicleClient.selectBestVehicle(
                    null,
                    plan.getStartLongitude() != null ? BigDecimal.valueOf(plan.getStartLongitude()) : null,
                    plan.getStartLatitude() != null ? BigDecimal.valueOf(plan.getStartLatitude()) : null,
                    plan.getPlannedCargoWeight(),
                    null
            );
            
            List<VehicleInfo> vehicles = result != null ? result.getData() : null;
            if (vehicles != null && !vehicles.isEmpty()) {
                VehicleInfo vehicle = vehicles.get(0);
                Long vehicleId = vehicle.getId();
                log.info("智能分配车辆成功, vehicleId: {}", vehicleId);
                return vehicleId;
            }
        } catch (Exception e) {
            log.error("智能分配车辆失败", e);
        }
        return null;
    }
    
    private Long assignVehicleForPlan(DispatchPlan plan, Integer planType) {
        if (planType == 1) {
            return assignVehicle(plan);
        }
        
        if (planType == 2) {
            Long faultVehicleId = assignFaultVehicle();
            if (faultVehicleId != null) {
                log.info("维修任务分配故障车辆成功, vehicleId: {}", faultVehicleId);
                return faultVehicleId;
            }
            
            Long maintenanceVehicleId = assignMaintenanceVehicle();
            if (maintenanceVehicleId != null) {
                log.info("维修任务分配维护中车辆成功, vehicleId: {}", maintenanceVehicleId);
                return maintenanceVehicleId;
            }
            
            Long availableVehicleId = assignRandomAvailableVehicle();
            if (availableVehicleId != null) {
                log.info("无故障/维护车辆，维修任务分配空闲车辆作为检修任务, vehicleId: {}", availableVehicleId);
                return availableVehicleId;
            }
            
            log.warn("维修任务无法分配任何车辆");
            return null;
        }
        
        if (planType == 3) {
            Long safetyOfficerVehicleId = assignSafetyOfficerVehicle();
            if (safetyOfficerVehicleId != null) {
                log.info("巡检任务分配救援专用车成功, vehicleId: {}", safetyOfficerVehicleId);
                return safetyOfficerVehicleId;
            }
            return null;
        }
        
        return assignRandomAvailableVehicle();
    }
    
    private Long assignFaultVehicle() {
        try {
            var result = vehicleClient.getFaultVehicles();
            List<VehicleInfo> vehicles = result != null ? result.getData() : null;
            if (vehicles != null && !vehicles.isEmpty()) {
                int randomIndex = (int) (Math.random() * vehicles.size());
                VehicleInfo vehicle = vehicles.get(randomIndex);
                return vehicle.getId();
            }
        } catch (Exception e) {
            log.error("获取故障车辆失败", e);
        }
        return null;
    }
    
    private Long assignMaintenanceVehicle() {
        try {
            var result = vehicleClient.getMaintenanceVehicles();
            List<VehicleInfo> vehicles = result != null ? result.getData() : null;
            if (vehicles != null && !vehicles.isEmpty()) {
                int randomIndex = (int) (Math.random() * vehicles.size());
                VehicleInfo vehicle = vehicles.get(randomIndex);
                return vehicle.getId();
            }
        } catch (Exception e) {
            log.error("获取维护中车辆失败", e);
        }
        return null;
    }
    
    private Long assignRandomAvailableVehicle() {
        try {
            var result = vehicleClient.getAvailableVehicles();
            List<VehicleInfo> vehicles = result != null ? result.getData() : null;
            if (vehicles != null && !vehicles.isEmpty()) {
                List<VehicleInfo> filteredVehicles = vehicles.stream()
                        .filter(v -> v.getVehicleType() == null || v.getVehicleType() != 9)
                        .collect(java.util.stream.Collectors.toList());
                if (!filteredVehicles.isEmpty()) {
                    int randomIndex = (int) (Math.random() * filteredVehicles.size());
                    VehicleInfo vehicle = filteredVehicles.get(randomIndex);
                    return vehicle.getId();
                }
            }
        } catch (Exception e) {
            log.error("随机分配车辆失败", e);
        }
        return null;
    }
    
    private Long assignRepairmanVehicle() {
        try {
            var result = vehicleClient.getRepairmanVehicles();
            List<VehicleInfo> vehicles = result != null ? result.getData() : null;
            if (vehicles != null && !vehicles.isEmpty()) {
                int randomIndex = (int) (Math.random() * vehicles.size());
                VehicleInfo vehicle = vehicles.get(randomIndex);
                log.info("分配维修专用车成功, vehicleId: {}, vehicleNo: {}", vehicle.getId(), vehicle.getVehicleNo());
                return vehicle.getId();
            }
            log.warn("没有可用的维修专用车，尝试分配普通车辆");
            return assignRandomAvailableVehicle();
        } catch (Exception e) {
            log.error("获取维修专用车失败", e);
            return assignRandomAvailableVehicle();
        }
    }
    
    private Long assignSafetyOfficerVehicle() {
        try {
            var result = vehicleClient.getSafetyOfficerVehicles();
            List<VehicleInfo> vehicles = result != null ? result.getData() : null;
            if (vehicles != null && !vehicles.isEmpty()) {
                int randomIndex = (int) (Math.random() * vehicles.size());
                VehicleInfo vehicle = vehicles.get(randomIndex);
                log.info("分配救援专用车成功, vehicleId: {}, vehicleNo: {}", vehicle.getId(), vehicle.getVehicleNo());
                return vehicle.getId();
            }
        } catch (Exception e) {
            log.error("获取救援专用车失败", e);
        }
        return null;
    }
    
    private void createTransportTask(DispatchPlan plan, int sequence, String taskNo, Long executorId, Long vehicleId, String priority) {
        TransportTask task = new TransportTask();
        task.setTaskNo(taskNo);
        task.setPlanId(plan.getId());
        task.setRouteId(plan.getRouteId());
        task.setTaskSequence(sequence);
        task.setVehicleId(vehicleId);
        task.setExecutorId(executorId);
        task.setStartLocation(plan.getStartLocation());
        task.setStartLongitude(plan.getStartLongitude());
        task.setStartLatitude(plan.getStartLatitude());
        task.setEndLocation(plan.getEndLocation());
        task.setEndLongitude(plan.getEndLongitude());
        task.setEndLatitude(plan.getEndLatitude());
        task.setCargoWeight(plan.getPlannedCargoWeight());
        task.setStatus(0);
        task.setPriority(priority != null ? priority : "normal");
        
        task.setScheduledStartTime(calculateScheduledStart(plan));
        task.setScheduledEndTime(calculateScheduledEnd(plan));
        
        transportTaskMapper.insert(task);
        log.info("创建运输任务成功，任务编号：{}，执行人：{}，车辆：{}", taskNo, executorId, vehicleId);
        
        sendTaskNotification(task, executorId);
    }
    
    private void createMaintenanceTask(DispatchPlan plan, int sequence, String taskNo, Long executorId, Long vehicleId, Long repairmanVehicleId, String priority) {
        MaintenanceTask task = new MaintenanceTask();
        task.setTaskNo(taskNo);
        task.setPlanId(plan.getId());
        task.setVehicleId(vehicleId);
        task.setRepairmanVehicleId(repairmanVehicleId);
        task.setExecutorId(executorId);
        task.setStatus(0);
        task.setPriority(priority != null ? priority : "normal");
        
        LocalDateTime scheduledStart = calculateScheduledStart(plan);
        task.setScheduledStartTime(scheduledStart);
        task.setScheduledEndTime(scheduledStart.plusHours(4));
        
        maintenanceTaskMapper.insert(task);
        log.info("创建维修任务成功，任务编号：{}，执行人：{}，待维修车辆：{}，维修员车辆：{}", taskNo, executorId, vehicleId, repairmanVehicleId);
        
        sendTaskNotification(task, executorId);
    }
    
    private void createInspectionTask(DispatchPlan plan, int sequence, String taskNo, Long executorId, Long vehicleId, String priority) {
        InspectionTask task = new InspectionTask();
        task.setTaskNo(taskNo);
        task.setPlanId(plan.getId());
        task.setVehicleId(vehicleId);
        task.setExecutorId(executorId);
        task.setStatus(0);
        task.setPriority(priority != null ? priority : "normal");
        
        LocalDateTime scheduledStart = calculateScheduledStart(plan);
        task.setScheduledStartTime(scheduledStart);
        task.setScheduledEndTime(scheduledStart.plusHours(4));
        
        inspectionTaskMapper.insert(task);
        log.info("创建巡检任务成功，任务编号：{}，执行人：{}，车辆：{}", taskNo, executorId, vehicleId);
        
        sendTaskNotification(task, executorId);
    }
    
    private void sendTaskNotification(Object task, Long executorId) {
        if (executorId == null) {
            log.warn("执行人ID为空，跳过消息推送");
            return;
        }
        
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("taskId", extractTaskId(task));
            message.put("taskNo", extractTaskNo(task));
            message.put("executorId", executorId);
            message.put("taskType", extractTaskType(task));
            message.put("createTime", LocalDateTime.now());
            message.put("type", "DISPATCH_TASK");
            
            messageProducer.sendMessage(
                    MqConstants.DISPATCH_TASK_EXCHANGE,
                    MqConstants.DISPATCH_TASK_ROUTING_KEY,
                    message
            );
            
            log.info("推送调度任务消息成功， taskId: {}, executorId: {}", extractTaskId(task), executorId);
        } catch (Exception e) {
            log.error("推送调度任务消息失败， taskId: {}, executorId: {}", extractTaskId(task), executorId, e);
        }
    }
    
    private Integer extractTaskType(Object task) {
        if (task instanceof TransportTask) {
            return 1;
        } else if (task instanceof MaintenanceTask) {
            return 2;
        } else if (task instanceof InspectionTask) {
            return 3;
        }
        return 1;
    }
    
    private Long extractTaskId(Object task) {
        if (task instanceof TransportTask) {
            return ((TransportTask) task).getId();
        } else if (task instanceof MaintenanceTask) {
            return ((MaintenanceTask) task).getId();
        } else if (task instanceof InspectionTask) {
            return ((InspectionTask) task).getId();
        }
        return null;
    }
    
    private String extractTaskNo(Object task) {
        if (task instanceof TransportTask) {
            return ((TransportTask) task).getTaskNo();
        } else if (task instanceof MaintenanceTask) {
            return ((MaintenanceTask) task).getTaskNo();
        } else if (task instanceof InspectionTask) {
            return ((InspectionTask) task).getTaskNo();
        }
        return null;
    }
    
    private LocalDateTime calculateScheduledStart(DispatchPlan plan) {
        String startTimeSlot = plan.getStartTimeSlot();
        
        if (startTimeSlot != null && !startTimeSlot.isEmpty()) {
            try {
                LocalDate startDate = LocalDate.parse(startTimeSlot);
                return startDate.atTime(8, 0);
            } catch (Exception e) {
                log.warn("解析开始日期失败: {}", startTimeSlot);
            }
        }
        
        LocalDate planDate = plan.getPlanDate();
        if (planDate != null) {
            return planDate.atTime(8, 0);
        }
        
        return LocalDate.now().atTime(8, 0);
    }
    
    private LocalDateTime calculateScheduledEnd(DispatchPlan plan) {
        String endTimeSlot = plan.getEndTimeSlot();
        
        if (endTimeSlot != null && !endTimeSlot.isEmpty()) {
            try {
                LocalDate endDate = LocalDate.parse(endTimeSlot);
                return endDate.atTime(18, 0);
            } catch (Exception e) {
                log.warn("解析截止日期失败: {}", endTimeSlot);
            }
        }
        
        LocalDate planDate = plan.getPlanDate();
        if (planDate != null) {
            return planDate.atTime(18, 0);
        }
        
        return LocalDate.now().atTime(18, 0);
    }
    
    private String generateTaskNo(Integer planType, int sequence) {
        String prefix;
        if (planType == null) {
            prefix = "TT";
        } else {
            prefix = switch (planType) {
                case 1 -> "TT";
                case 2 -> "MT";
                case 3 -> "IT";
                default -> "TT";
            };
        }
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return prefix + dateStr + String.format("%02d", sequence) + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        DispatchPlan plan = dispatchPlanMapper.selectById(id);
        if (plan == null) {
            throw new RuntimeException("调度计划不存在");
        }
        
        if (plan.getStatus() != 1) {
            throw new RuntimeException("只有执行中的计划可以完成");
        }
        
        plan.setStatus(2);
        dispatchPlanMapper.updateById(plan);
        log.info("完成调度计划成功，计划ID：{}", id);
    }

    private String generatePlanNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "DP" + dateStr + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private DispatchPlanVO convertToVO(DispatchPlan plan) {
        DispatchPlanVO vo = new DispatchPlanVO();
        BeanUtils.copyProperties(plan, vo);
        
        vo.setId(String.valueOf(plan.getId()));
        if (plan.getRouteId() != null) {
            vo.setRouteId(String.valueOf(plan.getRouteId()));
        }
        
        if (plan.getCompletedTrips() == null) {
            vo.setCompletedTrips(0);
        }
        
        if (plan.getRouteId() != null) {
            try {
                RouteTemplate route = routeTemplateMapper.selectById(plan.getRouteId());
                if (route != null) {
                    vo.setRouteName(route.getRouteName());
                    vo.setStartLocation(route.getStartLocation());
                    vo.setEndLocation(route.getEndLocation());
                }
            } catch (Exception e) {
                log.warn("获取路线信息失败，routeId: {}", plan.getRouteId());
            }
        }
        
        vo.setStatusName(getStatusName(plan.getStatus()));
        
        return vo;
    }
    
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待执行";
            case 1 -> "执行中";
            case 2 -> "已完成";
            case 3 -> "已取消";
            default -> "未知";
        };
    }
}
