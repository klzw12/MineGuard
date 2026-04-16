package com.klzw.service.vehicle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.klzw.common.core.client.CostClient;
import com.klzw.service.vehicle.dto.VehicleMaintenanceDTO;
import com.klzw.service.vehicle.entity.VehicleMaintenance;
import com.klzw.service.vehicle.mapper.VehicleMaintenanceMapper;
import com.klzw.service.vehicle.service.VehicleMaintenanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class VehicleMaintenanceServiceImpl extends ServiceImpl<VehicleMaintenanceMapper, VehicleMaintenance> implements VehicleMaintenanceService {
    
    @Resource
    private VehicleMaintenanceMapper vehicleMaintenanceMapper;

    @Resource
    private CostClient costClient;
    
    @Override
    public VehicleMaintenance addMaintenanceRecord(VehicleMaintenanceDTO maintenanceDTO) {
        log.info("添加车辆保养记录: vehicleId={}, maintenanceDate={}", maintenanceDTO.getVehicleId(), maintenanceDTO.getMaintenanceDate());
        VehicleMaintenance maintenance = new VehicleMaintenance();
        maintenance.setVehicleId(maintenanceDTO.getVehicleId());
        maintenance.setMaintenanceType(maintenanceDTO.getMaintenanceType());
        if (maintenanceDTO.getMaintenanceDate() != null) {
            maintenance.setMaintenanceDate(maintenanceDTO.getMaintenanceDate().atStartOfDay());
        }
        maintenance.setMaintenanceShop(maintenanceDTO.getMaintenanceShop());
        maintenance.setMaintenanceContent(maintenanceDTO.getMaintenanceContent());
        maintenance.setMaintenanceCost(maintenanceDTO.getMaintenanceCost());
        save(maintenance);

        // 调用CostClient添加成本明细
        try {
            Map<String, Object> costDetailRequest = new java.util.HashMap<>();
            costDetailRequest.put("costType", 2); // 维修成本
            costDetailRequest.put("costName", "车辆保养费用");
            costDetailRequest.put("amount", maintenanceDTO.getMaintenanceCost());
            costDetailRequest.put("vehicleId", maintenanceDTO.getVehicleId());
            costDetailRequest.put("costDate", maintenanceDTO.getMaintenanceDate() != null ? maintenanceDTO.getMaintenanceDate().toString() : null);
            costDetailRequest.put("description", "车辆保养: " + maintenanceDTO.getMaintenanceContent());
            costClient.addCostDetail(costDetailRequest);
            log.info("添加车辆保养成本明细成功: vehicleId={}, amount={}", maintenanceDTO.getVehicleId(), maintenanceDTO.getMaintenanceCost());
        } catch (Exception e) {
            log.error("添加车辆保养成本明细失败", e);
        }

        return maintenance;
    }
    
    @Override
    public List<VehicleMaintenance> getMaintenanceRecords(Long vehicleId, int page, int size) {
        log.info("获取车辆保养记录: vehicleId={}, page={}, size={}", vehicleId, page, size);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<VehicleMaintenance> pageObj = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VehicleMaintenance> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(VehicleMaintenance::getVehicleId, vehicleId)
               .orderByDesc(VehicleMaintenance::getMaintenanceDate);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<VehicleMaintenance> result = 
                getBaseMapper().selectPage(pageObj, wrapper);
        
        return result.getRecords();
    }
    
    @Override
    public List<VehicleMaintenance> getAllMaintenanceRecords(int page, int size) {
        log.info("获取所有保养记录: page={}, size={}", page, size);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<VehicleMaintenance> pageObj = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VehicleMaintenance> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.orderByDesc(VehicleMaintenance::getMaintenanceDate);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<VehicleMaintenance> result = 
                getBaseMapper().selectPage(pageObj, wrapper);
        
        return result.getRecords();
    }
    
    @Override
    public VehicleMaintenance getNextMaintenance(Long vehicleId) {
        log.info("获取车辆下次保养信息: vehicleId={}", vehicleId);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VehicleMaintenance> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(VehicleMaintenance::getVehicleId, vehicleId)
               .isNotNull(VehicleMaintenance::getNextMaintenanceDate)
               .orderByDesc(VehicleMaintenance::getNextMaintenanceDate)
               .last("LIMIT 1");
        
        return getOne(wrapper);
    }
    
}
