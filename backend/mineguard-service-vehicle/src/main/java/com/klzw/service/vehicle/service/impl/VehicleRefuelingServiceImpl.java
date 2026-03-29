package com.klzw.service.vehicle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.klzw.service.vehicle.dto.VehicleRefuelingDTO;
import com.klzw.service.vehicle.entity.VehicleRefueling;
import com.klzw.service.vehicle.mapper.VehicleRefuelingMapper;
import com.klzw.service.vehicle.service.VehicleRefuelingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class VehicleRefuelingServiceImpl extends ServiceImpl<VehicleRefuelingMapper, VehicleRefueling> implements VehicleRefuelingService {
    
    @Override
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
        return refueling;
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
