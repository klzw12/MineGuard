package com.klzw.service.vehicle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.klzw.service.vehicle.dto.VehicleFaultDTO;
import com.klzw.service.vehicle.entity.VehicleFault;
import com.klzw.service.vehicle.mapper.VehicleFaultMapper;
import com.klzw.service.vehicle.service.VehicleFaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class VehicleFaultServiceImpl extends ServiceImpl<VehicleFaultMapper, VehicleFault> implements VehicleFaultService {
    
    @Resource
    private VehicleFaultMapper vehicleFaultMapper;
    
    @Override
    public VehicleFault reportFault(VehicleFaultDTO faultDTO) {
        log.info("报告车辆故障: vehicleId={}, faultType={}", faultDTO.getVehicleId(), faultDTO.getFaultType());
        // 转换DTO为实体类
        VehicleFault fault = new VehicleFault();
        fault.setVehicleId(faultDTO.getVehicleId());
        fault.setFaultType(faultDTO.getFaultType());
        fault.setFaultDescription(faultDTO.getFaultDescription());
        fault.setFaultDate(faultDTO.getFaultDate() != null ? faultDTO.getFaultDate() : LocalDateTime.now());
        fault.setSeverity(faultDTO.getSeverity());
        fault.setStatus(faultDTO.getStatus() != null ? faultDTO.getStatus() : 1); // 1-未处理
        save(fault);
        return fault;
    }
    
    @Override
    public VehicleFault handleFault(Long id, Long repairmanId, String repairContent, java.math.BigDecimal repairCost) {
        log.info("处理车辆故障: id={}, repairmanId={}", id, repairmanId);
        VehicleFault fault = getById(id);
        if (fault == null) {
            throw new RuntimeException("故障记录不存在");
        }
        fault.setRepairmanId(repairmanId);
        fault.setRepairContent(repairContent);
        fault.setRepairCost(repairCost);
        fault.setRepairDate(LocalDateTime.now());
        fault.setStatus(3); // 3-已处理
        updateById(fault);
        return fault;
    }
    
    @Override
    public List<VehicleFault> getFaultRecords(Long vehicleId, Integer status, int page, int size) {
        log.info("获取车辆故障记录: vehicleId={}, status={}, page={}, size={}", vehicleId, status, page, size);
        // 这里可以使用MyBatis Plus的条件查询和分页
        // 暂时返回所有记录
        return list();
    }
    
}
