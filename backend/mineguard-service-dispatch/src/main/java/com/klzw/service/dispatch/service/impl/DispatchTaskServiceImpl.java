package com.klzw.service.dispatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.core.client.DriverClient;
import com.klzw.common.core.result.Result;
import com.klzw.service.dispatch.dto.DispatchTaskDTO;
import com.klzw.service.dispatch.entity.DispatchPlan;
import com.klzw.service.dispatch.entity.TransportTask;
import com.klzw.service.dispatch.mapper.DispatchPlanMapper;
import com.klzw.service.dispatch.mapper.TransportTaskMapper;
import com.klzw.service.dispatch.service.DispatchTaskService;
import com.klzw.service.dispatch.vo.DispatchTaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchTaskServiceImpl implements DispatchTaskService {

    private final TransportTaskMapper transportTaskMapper;
    private final DispatchPlanMapper dispatchPlanMapper;
    private final DriverClient driverClient;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DispatchTaskVO create(DispatchTaskDTO dto) {
        TransportTask entity = new TransportTask();
        BeanUtils.copyProperties(dto, entity);
        entity.setTaskNo(generateTaskNo());
        entity.setStatus(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        
        transportTaskMapper.insert(entity);
        log.info("创建运输任务：taskNo={}, vehicleId={}, executorId={}", 
            entity.getTaskNo(), entity.getVehicleId(), entity.getExecutorId());
        
        return convertToVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DispatchTaskVO update(DispatchTaskDTO dto) {
        TransportTask entity = transportTaskMapper.selectById(dto.getId());
        if (entity == null) {
            throw new RuntimeException("运输任务不存在");
        }
        
        BeanUtils.copyProperties(dto, entity, "id", "taskNo", "createTime");
        entity.setUpdateTime(LocalDateTime.now());
        
        transportTaskMapper.updateById(entity);
        log.info("更新运输任务：id={}", entity.getId());
        
        return convertToVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        transportTaskMapper.deleteById(id);
        log.info("删除运输任务：id={}", id);
    }

    @Override
    public DispatchTaskVO getById(Long id) {
        TransportTask entity = transportTaskMapper.selectById(id);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public List<DispatchTaskVO> getList(Integer status, Long executorId, Long vehicleId) {
        LambdaQueryWrapper<TransportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransportTask::getDeleted, 0);
        
        if (status != null) {
            wrapper.eq(TransportTask::getStatus, status);
        }
        if (executorId != null) {
            wrapper.eq(TransportTask::getExecutorId, executorId);
        }
        if (vehicleId != null) {
            wrapper.eq(TransportTask::getVehicleId, vehicleId);
        }
        
        wrapper.orderByDesc(TransportTask::getCreateTime);
        
        List<TransportTask> tasks = transportTaskMapper.selectList(wrapper);
        return tasks.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<DispatchTaskVO> getByPlanId(Long planId) {
        List<TransportTask> tasks = transportTaskMapper.findByPlanId(planId);
        return tasks.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<DispatchTaskVO> getPendingByVehicleId(Long vehicleId) {
        List<TransportTask> tasks = transportTaskMapper.findPendingByVehicleId(vehicleId);
        return tasks.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<DispatchTaskVO> getPendingByDriverId(Long driverId) {
        List<TransportTask> tasks = transportTaskMapper.findPendingByExecutorId(driverId);
        return tasks.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignVehicle(Long taskId, Long vehicleId) {
        TransportTask task = transportTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("运输任务不存在");
        }
        
        if (!checkVehicleAvailable(vehicleId, task.getScheduledStartTime(), task.getScheduledEndTime())) {
            throw new RuntimeException("车辆在该时间段已有其他任务");
        }
        
        task.setVehicleId(vehicleId);
        task.setUpdateTime(LocalDateTime.now());
        transportTaskMapper.updateById(task);
        log.info("分配车辆：taskId={}, vehicleId={}", taskId, vehicleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignDriver(Long taskId, Long driverId) {
        TransportTask task = transportTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("运输任务不存在");
        }
        
        if (!checkDriverAvailable(driverId, task.getScheduledStartTime(), task.getScheduledEndTime())) {
            throw new RuntimeException("司机在该时间段已有其他任务");
        }
        
        task.setExecutorId(driverId);
        task.setUpdateTime(LocalDateTime.now());
        transportTaskMapper.updateById(task);
        log.info("分配司机：taskId={}, driverId={}", taskId, driverId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startTask(Long taskId) {
        TransportTask task = transportTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("运输任务不存在");
        }
        
        task.setStatus(2);
        task.setActualStartTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        transportTaskMapper.updateById(task);
        log.info("开始任务：taskId={}", taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long taskId) {
        TransportTask task = transportTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("运输任务不存在");
        }
        
        task.setStatus(3);
        task.setActualEndTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        transportTaskMapper.updateById(task);
        log.info("完成任务：taskId={}", taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(Long taskId) {
        TransportTask task = transportTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("运输任务不存在");
        }
        
        task.setStatus(4);
        task.setUpdateTime(LocalDateTime.now());
        transportTaskMapper.updateById(task);
        log.info("取消任务：taskId={}", taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reassignTask(Long taskId, Long newVehicleId, Long newDriverId) {
        TransportTask task = transportTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("运输任务不存在");
        }
        
        if (newVehicleId != null && !newVehicleId.equals(task.getVehicleId())) {
            if (!checkVehicleAvailable(newVehicleId, task.getScheduledStartTime(), task.getScheduledEndTime())) {
                throw new RuntimeException("新车辆在该时间段已有其他任务");
            }
            task.setVehicleId(newVehicleId);
        }
        
        if (newDriverId != null && !newDriverId.equals(task.getExecutorId())) {
            if (!checkDriverAvailable(newDriverId, task.getScheduledStartTime(), task.getScheduledEndTime())) {
                throw new RuntimeException("新司机在该时间段已有其他任务");
            }
            task.setExecutorId(newDriverId);
        }
        
        task.setUpdateTime(LocalDateTime.now());
        transportTaskMapper.updateById(task);
        log.info("重新分配任务：taskId={}, newVehicleId={}, newDriverId={}", taskId, newVehicleId, newDriverId);
    }

    @Override
    public List<DispatchTaskVO> getAvailableTasksForReassignment(LocalDateTime startTime, LocalDateTime endTime) {
        List<TransportTask> tasks = transportTaskMapper.findByStatusAndTimeRange(0, startTime, endTime);
        return tasks.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public int countTasksByVehicle(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        return transportTaskMapper.countByVehicleIdAndTimeRange(vehicleId, startTime, endTime);
    }

    @Override
    public int countTasksByDriver(Long driverId, LocalDateTime startTime, LocalDateTime endTime) {
        return transportTaskMapper.countByExecutorIdAndTimeRange(driverId, startTime, endTime);
    }

    private boolean checkVehicleAvailable(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        int count = transportTaskMapper.countByVehicleIdAndTimeRange(vehicleId, startTime, endTime);
        return count == 0;
    }

    private boolean checkDriverAvailable(Long driverId, LocalDateTime startTime, LocalDateTime endTime) {
        int count = transportTaskMapper.countByExecutorIdAndTimeRange(driverId, startTime, endTime);
        return count == 0;
    }

    private String generateTaskNo() {
        return "TRANS" + LocalDateTime.now().format(FORMATTER);
    }

    private DispatchTaskVO convertToVO(TransportTask entity) {
        DispatchTaskVO vo = new DispatchTaskVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setDriverId(entity.getExecutorId());
        
        if (entity.getStatus() != null) {
            vo.setStatusName(getStatusName(entity.getStatus()));
        }
        
        if (entity.getPlanId() != null) {
            DispatchPlan plan = dispatchPlanMapper.selectById(entity.getPlanId());
            if (plan != null) {
                vo.setPlanName(plan.getPlanName());
            }
        }
        
        // 从司机服务获取司机信息
        if (entity.getExecutorId() != null) {
            try {
                Result<com.klzw.common.core.domain.dto.DriverInfo> driverResult = driverClient.getById(entity.getExecutorId());
                if (driverResult != null && driverResult.getCode() == 200 && driverResult.getData() != null) {
                    com.klzw.common.core.domain.dto.DriverInfo driverInfo = driverResult.getData();
                    vo.setDriverName(driverInfo.getDriverName());
                    vo.setExecutorName(driverInfo.getDriverName());
                }
            } catch (Exception e) {
                log.warn("获取司机信息失败：driverId={}, error={}", entity.getExecutorId(), e.getMessage());
            }
        }
        
        return vo;
    }

    private String getStatusName(Integer status) {
        switch (status) {
            case 0: return "待接单";
            case 1: return "已接单";
            case 2: return "执行中";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知";
        }
    }
}
