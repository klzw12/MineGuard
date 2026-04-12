package com.klzw.common.core.client;

import com.klzw.common.core.domain.dto.TripResponse;
import com.klzw.common.core.result.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;
import java.util.Map;

@HttpExchange
public interface DispatchClient {

    @GetExchange("/dispatch/task/driver/{driverId}/pending")
    List<Long> getPendingTaskIdsByDriverId(@PathVariable("driverId") Long driverId);

    @PostExchange("/dispatch/task/dynamic-adjust/user-leave")
    void reassignTasksByUserLeave(
        @RequestParam("userId") Long userId,
        @RequestParam("roleCode") String roleCode);

    @PostExchange("/dispatch/main/maintenance-task/from-fault")
    Long createMaintenanceTaskFromFault(@RequestBody Map<String, Object> faultInfo);

    @PutExchange("/dispatch/main/task/{taskId}/start")
    Result<Void> startTaskByTrip(@PathVariable("taskId") Long taskId);

    @PutExchange("/dispatch/main/task/{taskId}/complete")
    Result<Void> completeTaskByTrip(@PathVariable("taskId") Long taskId);

    @GetExchange("/dispatch/task/vehicle/{vehicleId}/active")
    Result<TripResponse> getActiveTripByVehicleId(@PathVariable("vehicleId") Long vehicleId);
}
