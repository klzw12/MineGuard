package com.klzw.common.core.client;

import com.klzw.common.core.result.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

@HttpExchange
public interface UserClient {

    @GetExchange("/user/exists/{id}")
    Result<Boolean> existsUser(@PathVariable("id") Long id);

    @GetExchange("/user/{id}")
    Result<Object> getUserById(@PathVariable("id") Long id);

    @PostExchange("/user/driver/best")
    Result<Object> selectBestDriver(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String scheduledTime);

    @GetExchange("/user/driver/available")
    Result<List<Object>> getAvailableDrivers();

    @GetExchange("/user/driver/available-repairmen")
    Result<List<Object>> getAvailableRepairmen();

    @GetExchange("/user/driver/available-safety-officers")
    Result<List<Object>> getAvailableSafetyOfficers();

    @GetExchange("/user/driver/{driverId}/common-vehicles")
    Result<List<Object>> getCommonVehicles(@PathVariable("driverId") Long driverId);

    @GetExchange("/user/leave-ids")
    Result<List<Long>> getLeaveUserIds();
}
