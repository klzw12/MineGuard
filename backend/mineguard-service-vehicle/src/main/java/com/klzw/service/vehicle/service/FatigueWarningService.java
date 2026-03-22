package com.klzw.service.vehicle.service;

public interface FatigueWarningService {
    
    void sendFatigueWarning(Long vehicleId, Long tripId, int drivingMinutes);
}
