package com.klzw.service.vehicle.task;

import com.klzw.service.vehicle.entity.VehicleInsurance;
import com.klzw.service.vehicle.entity.VehicleMaintenance;
import com.klzw.service.vehicle.service.VehicleInsuranceService;
import com.klzw.service.vehicle.service.VehicleMaintenanceService;
import com.klzw.service.vehicle.service.VehicleStatusPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleReminderTask {

    private final VehicleInsuranceService vehicleInsuranceService;
    private final VehicleMaintenanceService vehicleMaintenanceService;
    private final VehicleStatusPushService vehicleStatusPushService;

    private static final int INSURANCE_REMINDER_DAYS = 30;
    private static final int MAINTENANCE_REMINDER_DAYS = 7;

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkInsuranceExpiry() {
        log.info("开始检查保险到期提醒...");
        
        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(INSURANCE_REMINDER_DAYS);
        
        List<VehicleInsurance> allInsurances = vehicleInsuranceService.list();
        
        for (VehicleInsurance insurance : allInsurances) {
            if (insurance.getExpiryDate() != null) {
                long daysUntilExpiry = ChronoUnit.DAYS.between(today, insurance.getExpiryDate());
                
                if (daysUntilExpiry >= 0 && daysUntilExpiry <= INSURANCE_REMINDER_DAYS) {
                    String warningMessage = String.format(
                        "车辆保险即将到期！车辆ID: %d, 保险公司: %s, 到期日期: %s, 剩余%d天",
                        insurance.getVehicleId(),
                        insurance.getInsuranceCompany(),
                        insurance.getExpiryDate(),
                        daysUntilExpiry
                    );
                    
                    log.warn(warningMessage);
                    
                    vehicleStatusPushService.pushVehicleWarning(
                        insurance.getVehicleId(),
                        "insurance_expiry",
                        warningMessage
                    );
                }
            }
        }
        
        log.info("保险到期提醒检查完成");
    }

    @Scheduled(cron = "0 0 10 * * ?")
    public void checkMaintenanceDue() {
        log.info("开始检查维护到期提醒...");
        
        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(MAINTENANCE_REMINDER_DAYS);
        
        List<VehicleMaintenance> maintenances = vehicleMaintenanceService.list();
        
        for (VehicleMaintenance maintenance : maintenances) {
            if (maintenance.getNextMaintenanceDate() != null) {
                long daysUntilMaintenance = ChronoUnit.DAYS.between(today, maintenance.getNextMaintenanceDate());
                
                if (daysUntilMaintenance >= 0 && daysUntilMaintenance <= MAINTENANCE_REMINDER_DAYS) {
                    String warningMessage = String.format(
                        "车辆维护即将到期！车辆ID: %d, 下次维护日期: %s, 剩余%d天",
                        maintenance.getVehicleId(),
                        maintenance.getNextMaintenanceDate(),
                        daysUntilMaintenance
                    );
                    
                    log.warn(warningMessage);
                    
                    vehicleStatusPushService.pushVehicleWarning(
                        maintenance.getVehicleId(),
                        "maintenance_due",
                        warningMessage
                    );
                }
            }
        }
        
        log.info("维护到期提醒检查完成");
    }
}
