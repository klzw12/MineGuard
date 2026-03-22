package com.klzw.service.vehicle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.klzw.service.vehicle.dto.VehicleInsuranceDTO;
import com.klzw.service.vehicle.entity.VehicleInsurance;
import com.klzw.service.vehicle.mapper.VehicleInsuranceMapper;
import com.klzw.service.vehicle.service.VehicleInsuranceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class VehicleInsuranceServiceImpl extends ServiceImpl<VehicleInsuranceMapper, VehicleInsurance> implements VehicleInsuranceService {
    
    @Override
    public VehicleInsurance addInsurance(VehicleInsuranceDTO insuranceDTO) {
        log.info("添加车辆保险信息: vehicleId={}, insuranceCompany={}", insuranceDTO.getVehicleId(), insuranceDTO.getInsuranceCompany());
        VehicleInsurance insurance = new VehicleInsurance();
        insurance.setVehicleId(insuranceDTO.getVehicleId());
        insurance.setInsuranceCompany(insuranceDTO.getInsuranceCompany());
        insurance.setInsuranceNumber(insuranceDTO.getInsuranceNumber());
        insurance.setInsuranceType(insuranceDTO.getInsuranceType());
        insurance.setInsuranceAmount(insuranceDTO.getInsuranceAmount());
        insurance.setStartDate(insuranceDTO.getStartDate());
        insurance.setExpiryDate(insuranceDTO.getExpiryDate());
        insurance.setRemark(insuranceDTO.getRemark());
        
        LocalDate now = LocalDate.now();
        if (now.isAfter(insurance.getExpiryDate())) {
            insurance.setStatus(2);
        } else {
            insurance.setStatus(1);
        }
        save(insurance);
        return insurance;
    }
    
    @Override
    public List<VehicleInsurance> getVehicleInsurance(Long vehicleId) {
        log.info("获取车辆保险信息: vehicleId={}", vehicleId);
        LambdaQueryWrapper<VehicleInsurance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleInsurance::getVehicleId, vehicleId)
               .orderByDesc(VehicleInsurance::getExpiryDate);
        return list(wrapper);
    }
    
    @Override
    public VehicleInsurance getCurrentInsurance(Long vehicleId) {
        log.info("获取车辆当前有效的保险信息: vehicleId={}", vehicleId);
        LocalDate now = LocalDate.now();
        LambdaQueryWrapper<VehicleInsurance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleInsurance::getVehicleId, vehicleId)
               .eq(VehicleInsurance::getStatus, 1)
               .ge(VehicleInsurance::getExpiryDate, now)
               .orderByAsc(VehicleInsurance::getExpiryDate)
               .last("LIMIT 1");
        return getOne(wrapper);
    }
    
}
