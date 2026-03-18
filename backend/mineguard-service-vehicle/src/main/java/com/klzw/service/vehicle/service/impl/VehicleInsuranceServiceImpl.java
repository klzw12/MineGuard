package com.klzw.service.vehicle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.klzw.service.vehicle.dto.VehicleInsuranceDTO;
import com.klzw.service.vehicle.entity.VehicleInsurance;
import com.klzw.service.vehicle.mapper.VehicleInsuranceMapper;
import com.klzw.service.vehicle.service.VehicleInsuranceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class VehicleInsuranceServiceImpl extends ServiceImpl<VehicleInsuranceMapper, VehicleInsurance> implements VehicleInsuranceService {
    
    @Resource
    private VehicleInsuranceMapper vehicleInsuranceMapper;
    
    @Override
    public VehicleInsurance addInsurance(VehicleInsuranceDTO insuranceDTO) {
        log.info("添加车辆保险信息: vehicleId={}, insuranceCompany={}", insuranceDTO.getVehicleId(), insuranceDTO.getInsuranceCompany());
        // 转换DTO为实体类
        VehicleInsurance insurance = new VehicleInsurance();
        insurance.setVehicleId(insuranceDTO.getVehicleId());
        insurance.setInsuranceCompany(insuranceDTO.getInsuranceCompany());
        insurance.setInsuranceNumber(insuranceDTO.getInsuranceNumber());
        insurance.setInsuranceType(insuranceDTO.getInsuranceType());
        insurance.setInsuranceAmount(insuranceDTO.getInsuranceAmount());
        insurance.setStartDate(insuranceDTO.getStartDate());
        insurance.setExpiryDate(insuranceDTO.getExpiryDate());
        insurance.setRemark(insuranceDTO.getRemark());
        
        // 设置保险状态
        LocalDate now = LocalDate.now();
        if (now.isAfter(insurance.getExpiryDate())) {
            insurance.setStatus(2); // 2-过期
        } else {
            insurance.setStatus(1); // 1-有效
        }
        save(insurance);
        return insurance;
    }
    
    @Override
    public List<VehicleInsurance> getVehicleInsurance(Long vehicleId) {
        log.info("获取车辆保险信息: vehicleId={}", vehicleId);
        // 这里可以使用MyBatis Plus的条件查询
        // 暂时返回所有记录
        return list();
    }
    
    @Override
    public VehicleInsurance getCurrentInsurance(Long vehicleId) {
        log.info("获取车辆当前有效的保险信息: vehicleId={}", vehicleId);
        // 这里可以查询当前有效的保险信息
        // 暂时返回null
        return null;
    }
    
}
