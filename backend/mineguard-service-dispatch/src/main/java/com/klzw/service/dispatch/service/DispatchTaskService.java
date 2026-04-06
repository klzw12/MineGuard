package com.klzw.service.dispatch.service;

import com.klzw.service.dispatch.dto.DispatchTaskDTO;
import com.klzw.service.dispatch.vo.DispatchTaskVO;

import java.time.LocalDateTime;
import java.util.List;

public interface DispatchTaskService {

    DispatchTaskVO create(DispatchTaskDTO dto);

    DispatchTaskVO update(DispatchTaskDTO dto);

    void delete(Long id);

    DispatchTaskVO getById(Long id);

    List<DispatchTaskVO> getList(Integer status, Long executorId, Long vehicleId);

    List<DispatchTaskVO> getByPlanId(Long planId);

    List<DispatchTaskVO> getPendingByVehicleId(Long vehicleId);

    List<DispatchTaskVO> getPendingByDriverId(Long driverId);

    void assignVehicle(Long taskId, Long vehicleId);

    void assignDriver(Long taskId, Long driverId);

    void startTask(Long taskId);

    void completeTask(Long taskId);

    void cancelTask(Long taskId);

    void reassignTask(Long taskId, Long newVehicleId, Long newDriverId);

    List<DispatchTaskVO> getAvailableTasksForReassignment(LocalDateTime startTime, LocalDateTime endTime);

    int countTasksByVehicle(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime);

    int countTasksByDriver(Long driverId, LocalDateTime startTime, LocalDateTime endTime);
}
