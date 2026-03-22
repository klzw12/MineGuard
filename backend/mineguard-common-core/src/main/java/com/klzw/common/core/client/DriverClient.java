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

    @GetExchange("/api/driver/{id}")
    DriverInfo getById(@PathVariable("id") Long id);

    @GetExchange("/api/driver/user/{userId}")
    DriverInfo getByUserId(@PathVariable("userId") Long userId);

    @GetExchange("/api/driver/available")
    List<DriverInfo> getAvailableDrivers();

    @GetExchange("/api/driver/available-repairmen")
    List<DriverInfo> getAvailableRepairmen();

    @GetExchange("/api/driver/available-safety-officers")
    List<DriverInfo> getAvailableSafetyOfficers();

    @PostExchange("/api/driver/best")
    DriverInfo selectBestDriver(
        @RequestParam(required = false) Long vehicleId,
        @RequestParam(required = false) String scheduledTime);

    @GetExchange("/api/driver/{driverId}/common-vehicles")
    List<DriverVehicleInfo> getCommonVehicles(@PathVariable("driverId") Long driverId);
}
