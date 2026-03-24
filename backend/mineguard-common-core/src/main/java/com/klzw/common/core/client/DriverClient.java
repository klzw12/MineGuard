package com.klzw.common.core.client;

import com.klzw.common.core.domain.dto.DriverInfo;
import com.klzw.common.core.domain.dto.DriverVehicleInfo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange
public interface DriverClient {

    @GetExchange("/user/driver/{id}")
    DriverInfo getById(@PathVariable("id") Long id);

    @GetExchange("/user/driver/user/{userId}")
    DriverInfo getByUserId(@PathVariable("userId") Long userId);

    @GetExchange("/user/driver/available")
    List<DriverInfo> getAvailableDrivers();

    @GetExchange("/user/driver/available-repairmen")
    List<DriverInfo> getAvailableRepairmen();

    @GetExchange("/user/driver/available-safety-officers")
    List<DriverInfo> getAvailableSafetyOfficers();

    @PostExchange("/user/driver/best")
    DriverInfo selectBestDriver(
        @RequestParam(required = false) Long vehicleId,
        @RequestParam(required = false) String scheduledTime);

    @GetExchange("/user/driver/{driverId}/common-vehicles")
    List<DriverVehicleInfo> getCommonVehicles(@PathVariable("driverId") Long driverId);
    
    @GetExchange("/user/driver/ids")
    List<Long> getDriverIds();
}
