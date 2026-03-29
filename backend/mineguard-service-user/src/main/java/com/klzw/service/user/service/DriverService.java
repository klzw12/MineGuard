package com.klzw.service.user.service;

import com.klzw.service.user.dto.DriverDTO;
import com.klzw.service.user.vo.DriverVehicleVO;
import com.klzw.service.user.vo.DriverVO;

import java.util.List;

public interface DriverService {

    DriverVO getById(Long id);

    DriverVO getByUserId(Long userId);

    List<DriverVO> getList(String driverName, Integer status);

    void updateStatus(Long driverId, Integer status);

    List<DriverVO> getAvailableDrivers();

    List<DriverVO> getAvailableRepairmen();

    List<DriverVO> getAvailableSafetyOfficers();

    DriverVO selectBestDriver(Long vehicleId, String scheduledTime);

    void addCommonVehicle(Long driverId, Long vehicleId);

    void removeCommonVehicle(Long driverId, Long vehicleId);

    void setDefaultVehicle(Long driverId, Long vehicleId);

    List<DriverVehicleVO> getCommonVehicles(Long driverId);

    void incrementVehicleUseCount(Long driverId, Long vehicleId);

    List<Long> getDriverIds();
}
