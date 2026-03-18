package com.klzw.service.vehicle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.klzw.service.vehicle.dto.VehicleRefuelingDTO;
import com.klzw.service.vehicle.entity.VehicleRefueling;
import com.klzw.service.vehicle.mapper.VehicleRefuelingMapper;
import com.klzw.service.vehicle.service.VehicleRefuelingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class VehicleRefuelingServiceImpl extends ServiceImpl<VehicleRefuelingMapper, VehicleRefueling> implements VehicleRefuelingService {
    
    @Resource
    private VehicleRefuelingMapper vehicleRefuelingMapper;
    
    @Override
    public VehicleRefueling addRefuelingRecord(VehicleRefuelingDTO refuelingDTO) {
        log.info("添加车辆加油记录: vehicleId={}, refuelingDate={}", refuelingDTO.getVehicleId(), refuelingDTO.getRefuelingDate());
        // 转换DTO为实体类
        VehicleRefueling refueling = new VehicleRefueling();
        refueling.setVehicleId(refuelingDTO.getVehicleId());
        refueling.setDriverId(refuelingDTO.getDriverId());
        refueling.setRefuelingDate(refuelingDTO.getRefuelingDate());
        refueling.setFuelType(refuelingDTO.getFuelType());
        refueling.setFuelAmount(refuelingDTO.getFuelAmount());
        refueling.setFuelPrice(refuelingDTO.getFuelPrice());
        refueling.setTotalCost(refuelingDTO.getTotalCost());
        refueling.setMileage(refuelingDTO.getMileage());
        refueling.setGasStation(refuelingDTO.getGasStation());
        refueling.setRemark(refuelingDTO.getRemark());
        save(refueling);
        return refueling;
    }
    
    @Override
    public List<VehicleRefueling> getRefuelingRecords(Long vehicleId, int page, int size) {
        log.info("获取车辆加油记录: vehicleId={}, page={}, size={}", vehicleId, page, size);
        // 这里可以使用MyBatis Plus的分页查询
        // 暂时返回所有记录
        return list();
    }
    
}
