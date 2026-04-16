package com.klzw.service.vehicle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.klzw.common.core.client.CostClient;
import com.klzw.service.vehicle.dto.VehicleRefuelingDTO;
import com.klzw.service.vehicle.entity.VehicleRefueling;
import com.klzw.service.vehicle.entity.VehicleStatus;
import com.klzw.service.vehicle.mapper.VehicleRefuelingMapper;
import com.klzw.service.vehicle.service.VehicleRefuelingService;
import com.klzw.service.vehicle.service.VehicleStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleRefuelingServiceImpl extends ServiceImpl<VehicleRefuelingMapper, VehicleRefueling> implements VehicleRefuelingService {
    
    private final VehicleStatusService vehicleStatusService;
    
    @Resource
    private CostClient costClient;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public VehicleRefueling addRefuelingRecord(VehicleRefuelingDTO refuelingDTO) {
        log.info("添加车辆加油记录: vehicleId={}, refuelingDate={}", refuelingDTO.getVehicleId(), refuelingDTO.getRefuelingDate());
        VehicleRefueling refueling = new VehicleRefueling();
        refueling.setVehicleId(refuelingDTO.getVehicleId());
        refueling.setDriverId(refuelingDTO.getDriverId());
        if (refuelingDTO.getRefuelingDate() != null) {
            refueling.setRefuelingDate(refuelingDTO.getRefuelingDate().atStartOfDay());
        }
        refueling.setRefuelingStation(refuelingDTO.getRefuelingStation());
        refueling.setFuelType(refuelingDTO.getFuelType());
        refueling.setRefuelingAmount(refuelingDTO.getRefuelingAmount());
        refueling.setUnitPrice(refuelingDTO.getUnitPrice());
        refueling.setTotalCost(refuelingDTO.getTotalCost());
        refueling.setCurrentMileage(refuelingDTO.getCurrentMileage());
        save(refueling);
        
        updateVehicleFuelLevel(refuelingDTO.getVehicleId(), refuelingDTO.getRefuelingAmount());

        // 调用CostClient添加成本明细
        try {
            Map<String, Object> costDetailRequest = new java.util.HashMap<>();
            costDetailRequest.put("costType", 1); // 燃油成本
            costDetailRequest.put("costName", "车辆加油费用");
            costDetailRequest.put("amount", refuelingDTO.getTotalCost());
            costDetailRequest.put("vehicleId", refuelingDTO.getVehicleId());
            costDetailRequest.put("userId", refuelingDTO.getDriverId());
            costDetailRequest.put("costDate", refuelingDTO.getRefuelingDate() != null ? refuelingDTO.getRefuelingDate().toString() : null);
            costDetailRequest.put("description", "车辆加油: " + refuelingDTO.getRefuelingStation());
            costClient.addCostDetail(costDetailRequest);
            log.info("添加车辆加油成本明细成功: vehicleId={}, amount={}", refuelingDTO.getVehicleId(), refuelingDTO.getTotalCost());
        } catch (Exception e) {
            log.error("添加车辆加油成本明细失败", e);
        }
        
        return refueling;
    }
    
    private void updateVehicleFuelLevel(Long vehicleId, java.math.BigDecimal refuelingAmount) {
        try {
            VehicleStatus vehicleStatus = vehicleStatusService.getByVehicleId(vehicleId);
            if (vehicleStatus != null) {
                Integer currentFuelLevel = vehicleStatus.getFuelLevel();
                int addedFuelLevel = refuelingAmount != null ? refuelingAmount.intValue() : 0;
                int newFuelLevel = Math.min(100, (currentFuelLevel != null ? currentFuelLevel : 0) + addedFuelLevel);
                vehicleStatus.setFuelLevel(newFuelLevel);
                vehicleStatusService.updateById(vehicleStatus);
                log.info("更新车辆油量: vehicleId={}, 原油量={}, 加油量={}, 新油量={}", 
                    vehicleId, currentFuelLevel, addedFuelLevel, newFuelLevel);
            } else {
                log.warn("未找到车辆状态记录，无法更新油量: vehicleId={}", vehicleId);
            }
        } catch (Exception e) {
            log.error("更新车辆油量失败: vehicleId={}", vehicleId, e);
        }
    }
    
    @Override
    public List<VehicleRefueling> getRefuelingRecords(Long vehicleId, int page, int size) {
        log.info("获取车辆加油记录: vehicleId={}, page={}, size={}", vehicleId, page, size);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<VehicleRefueling> pageObj = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VehicleRefueling> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(VehicleRefueling::getVehicleId, vehicleId)
               .orderByDesc(VehicleRefueling::getRefuelingDate);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<VehicleRefueling> result = 
                getBaseMapper().selectPage(pageObj, wrapper);
        
        return result.getRecords();
    }
    
    @Override
    public List<VehicleRefueling> getAllRefuelingRecords(int page, int size) {
        log.info("获取所有加油记录: page={}, size={}", page, size);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<VehicleRefueling> pageObj = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VehicleRefueling> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.orderByDesc(VehicleRefueling::getRefuelingDate);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<VehicleRefueling> result = 
                getBaseMapper().selectPage(pageObj, wrapper);
        
        return result.getRecords();
    }
    
}
