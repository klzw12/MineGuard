package com.klzw.service.vehicle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.core.client.DispatchClient;
import com.klzw.service.vehicle.dto.VehicleFaultDTO;
import com.klzw.service.vehicle.dto.VehicleFaultStatisticsResponseDTO;
import com.klzw.service.vehicle.entity.VehicleFault;
import com.klzw.service.vehicle.enums.FaultStatusEnum;
import com.klzw.service.vehicle.enums.VehicleStatusEnum;
import com.klzw.service.vehicle.mapper.VehicleFaultMapper;
import com.klzw.service.vehicle.service.VehicleFaultService;
import com.klzw.service.vehicle.service.VehicleService;
import com.klzw.service.vehicle.exception.VehicleException;
import com.klzw.service.vehicle.exception.VehicleResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VehicleFaultServiceImpl extends ServiceImpl<VehicleFaultMapper, VehicleFault> implements VehicleFaultService {
    
    @Resource
    private VehicleFaultMapper vehicleFaultMapper;
    
    @Resource
    private VehicleService vehicleService;
    
    @Resource
    private DispatchClient dispatchClient;
    
    @Override
    public VehicleFault reportFault(VehicleFaultDTO faultDTO) {
        log.info("报告车辆故障: vehicleId={}, faultType={}, lat={}, lng={}", faultDTO.getVehicleId(), faultDTO.getFaultType(), faultDTO.getLatitude(), faultDTO.getLongitude());
        VehicleFault fault = new VehicleFault();
        fault.setVehicleId(faultDTO.getVehicleId());
        fault.setFaultType(faultDTO.getFaultType());
        fault.setFaultDescription(faultDTO.getFaultDescription());
        fault.setFaultDate(LocalDateTime.now());
        fault.setSeverity(faultDTO.getSeverity() != null ? faultDTO.getSeverity() : 1);
        fault.setStatus(FaultStatusEnum.PENDING.getCode());
        fault.setLatitude(faultDTO.getLatitude());
        fault.setLongitude(faultDTO.getLongitude());
        fault.setLocationAddress(faultDTO.getLocationAddress());
        save(fault);
        
        vehicleService.updateVehicleStatus(faultDTO.getVehicleId(), VehicleStatusEnum.FAULT.getCode());
        log.info("车辆状态已更新为故障: vehicleId={}", faultDTO.getVehicleId());
        
        return fault;
    }
    
    @Override
    public VehicleFault handleFault(Long id, Long repairmanId, String repairContent, java.math.BigDecimal repairCost) {
        log.info("处理车辆故障: id={}, repairmanId={}", id, repairmanId);
        VehicleFault fault = getById(id);
        if (fault == null) {
            throw new VehicleException(VehicleResultCode.FAULT_NOT_FOUND, "故障记录不存在：" + id);
        }
        fault.setRepairmanId(repairmanId);
        fault.setRepairContent(repairContent);
        fault.setRepairCost(repairCost);
        fault.setRepairDate(LocalDateTime.now());
        fault.setStatus(FaultStatusEnum.RESOLVED.getCode());
        updateById(fault);
        
        vehicleService.updateVehicleStatus(fault.getVehicleId(), VehicleStatusEnum.IDLE.getCode());
        log.info("车辆状态已恢复为空闲: vehicleId={}", fault.getVehicleId());
        
        return fault;
    }
    
    @Override
    public List<VehicleFault> getFaultRecords(Long vehicleId, Integer status, int page, int size) {
        log.info("获取车辆故障记录：vehicleId={}, status={}, page={}, size={}", vehicleId, status, page, size);
        
        Page<VehicleFault> pageObj = new Page<>(page, size);
        
        LambdaQueryWrapper<VehicleFault> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleFault::getVehicleId, vehicleId);
        if (status != null) {
            wrapper.eq(VehicleFault::getStatus, status);
        }
        wrapper.orderByDesc(VehicleFault::getFaultDate);
        
        Page<VehicleFault> result = getBaseMapper().selectPage(pageObj, wrapper);
        
        return result.getRecords();
    }
    
    @Override
    public List<VehicleFault> getAllFaultRecords(Integer status, int page, int size) {
        log.info("获取所有故障记录：status={}, page={}, size={}", status, page, size);
        
        Page<VehicleFault> pageObj = new Page<>(page, size);
        
        LambdaQueryWrapper<VehicleFault> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(VehicleFault::getStatus, status);
        }
        wrapper.orderByDesc(VehicleFault::getFaultDate);
        
        Page<VehicleFault> result = getBaseMapper().selectPage(pageObj, wrapper);
        
        return result.getRecords();
    }
    
    @Override
    public VehicleFaultStatisticsResponseDTO getFaultStatistics(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        log.info("按日期范围查询故障统计：startDate={}, endDate={}", startDate, endDate);
        
        // 构建查询条件
        LambdaQueryWrapper<VehicleFault> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(VehicleFault::getFaultDate, start.atStartOfDay())
               .le(VehicleFault::getFaultDate, end.atTime(23, 59, 59));
        
        List<VehicleFault> faults = list(wrapper);
        
        VehicleFaultStatisticsResponseDTO dto = new VehicleFaultStatisticsResponseDTO();
        dto.setStartDate(start);
        dto.setEndDate(end);
        
        if (faults == null || faults.isEmpty()) {
            dto.setFaultCount(0);
            dto.setMinorFaultCount(0);
            dto.setMajorFaultCount(0);
            dto.setCriticalFaultCount(0);
            dto.setTotalRepairCost(BigDecimal.ZERO);
            dto.setAvgRepairTime(BigDecimal.ZERO);
            dto.setRepairedCount(0);
            dto.setPendingCount(0);
            dto.setFaultTypeDistribution(new ArrayList<>());
            return dto;
        }
        
        // 统计各项数据
        int faultCount = faults.size();
        int minorFaultCount = 0;
        int majorFaultCount = 0;
        int criticalFaultCount = 0;
        BigDecimal totalRepairCost = BigDecimal.ZERO;
        int repairedCount = 0;
        int pendingCount = 0;
        Map<String, Integer> faultTypeCountMap = new HashMap<>();
        
        for (VehicleFault fault : faults) {
            // 按故障级别统计
            Integer severity = fault.getSeverity();
            if (severity != null) {
                if (severity == 1) {
                    minorFaultCount++;
                } else if (severity == 2) {
                    majorFaultCount++;
                } else if (severity == 3) {
                    criticalFaultCount++;
                }
            }
            
            // 统计维修成本
            if (fault.getRepairCost() != null) {
                totalRepairCost = totalRepairCost.add(fault.getRepairCost());
            }
            
            // 统计状态
            Integer status = fault.getStatus();
            if (status != null && status == 3) {
                repairedCount++;
            } else {
                pendingCount++;
            }
            
            // 统计故障类型
            String faultType = fault.getFaultType() != null ? fault.getFaultType() : "未知";
            faultTypeCountMap.put(faultType, faultTypeCountMap.getOrDefault(faultType, 0) + 1);
        }
        
        // 计算平均维修时长
        BigDecimal avgRepairTime = BigDecimal.ZERO;
        if (repairedCount > 0) {
            long totalRepairMinutes = faults.stream()
                .filter(f -> f.getRepairDate() != null && f.getFaultDate() != null)
                .mapToLong(f -> java.time.Duration.between(f.getFaultDate(), f.getRepairDate()).toMinutes())
                .sum();
            avgRepairTime = BigDecimal.valueOf(totalRepairMinutes).divide(BigDecimal.valueOf(repairedCount), 2, BigDecimal.ROUND_HALF_UP);
        }
        
        // 计算故障类型分布
        List<VehicleFaultStatisticsResponseDTO.FaultTypeDistribution> faultTypeDistribution = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : faultTypeCountMap.entrySet()) {
            VehicleFaultStatisticsResponseDTO.FaultTypeDistribution distribution = new VehicleFaultStatisticsResponseDTO.FaultTypeDistribution();
            distribution.setFaultType(entry.getKey());
            distribution.setCount(entry.getValue());
            distribution.setPercentage(BigDecimal.valueOf(entry.getValue())
                .divide(BigDecimal.valueOf(faultCount), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100)));
            faultTypeDistribution.add(distribution);
        }
        
        dto.setFaultCount(faultCount);
        dto.setMinorFaultCount(minorFaultCount);
        dto.setMajorFaultCount(majorFaultCount);
        dto.setCriticalFaultCount(criticalFaultCount);
        dto.setTotalRepairCost(totalRepairCost);
        dto.setAvgRepairTime(avgRepairTime);
        dto.setRepairedCount(repairedCount);
        dto.setPendingCount(pendingCount);
        dto.setFaultTypeDistribution(faultTypeDistribution);
        
        log.info("故障统计完成：faultCount={}, minorFaultCount={}, majorFaultCount={}, criticalFaultCount={}", 
            faultCount, minorFaultCount, majorFaultCount, criticalFaultCount);
        
        return dto;
    }
    
}
