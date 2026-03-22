package com.klzw.service.dispatch.service;

import com.klzw.service.dispatch.entity.TransportTask;

import java.time.LocalDateTime;
import java.util.List;

public interface DispatchService {

    TransportTask createDispatchTask(TransportTask task);

    TransportTask updateDispatchTask(TransportTask task);

    boolean executeDispatch(Long taskId);

    boolean cancelDispatchTask(Long taskId);

    TransportTask getDispatchTask(Long taskId);

    List<TransportTask> getDispatchTaskList(Integer status, LocalDateTime startDate, LocalDateTime endDate);

    void dynamicAdjustForVehicleFault(Long vehicleId);

    void dynamicAdjustForDriverLeave(Long driverId);

    void dynamicAdjustForUserLeave(Long userId, String roleCode);

    void dynamicAdjustForRepairmanLeave(Long repairmanId);

    void dynamicAdjustForSafetyOfficerLeave(Long safetyOfficerId);

    void reassignTasksByUserLeave(Long userId, String roleCode);

    void dynamicAdjustForRouteBlock(Long routeId);
}
