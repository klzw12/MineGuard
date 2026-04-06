package com.klzw.common.core.client;

import com.klzw.common.core.domain.dto.DriverInfo;
import com.klzw.common.core.domain.dto.DriverVehicleInfo;
import com.klzw.common.core.result.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange
public interface DriverClient {

    @GetExchange("/user/driver/{id}")
    Result<DriverInfo> getById(@PathVariable("id") Long id);

    @GetExchange("/user/driver/user/{userId}")
    Result<DriverInfo> getByUserId(@PathVariable("userId") Long userId);

    @GetExchange("/user/driver/available")
    Result<List<DriverInfo>> getAvailableDrivers();

    @GetExchange("/user/driver/available-repairmen")
    Result<List<DriverInfo>> getAvailableRepairmen();

    @GetExchange("/user/driver/available-safety-officers")
    Result<List<DriverInfo>> getAvailableSafetyOfficers();

    @PostExchange("/user/driver/best")
    Result<DriverInfo> selectBestDriver(
        @RequestParam(required = false) Long vehicleId,
        @RequestParam(required = false) String scheduledTime);

    @GetExchange("/user/driver/{driverId}/common-vehicles")
    Result<List<DriverVehicleInfo>> getCommonVehicles(@PathVariable("driverId") Long driverId);
    
    @GetExchange("/user/driver/ids")
    Result<List<Long>> getDriverIds();
}
