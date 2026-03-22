package com.klzw.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.service.user.entity.Driver;
import com.klzw.service.user.entity.DriverVehicle;
import com.klzw.service.user.mapper.DriverMapper;
import com.klzw.service.user.mapper.DriverVehicleMapper;
import com.klzw.service.user.service.DriverService;
import com.klzw.service.user.vo.DriverVehicleVO;
import com.klzw.service.user.vo.DriverVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverMapper driverMapper;
    private final DriverVehicleMapper driverVehicleMapper;
    private final RestTemplate restTemplate;

    @Override
    public DriverVO getById(Long id) {
        Driver driver = driverMapper.selectById(id);
        if (driver == null) {
            return null;
        }
        DriverVO vo = convertToVO(driver);
        vo.setCommonVehicles(getCommonVehicles(id));
        return vo;
    }

    @Override
    public DriverVO getByUserId(Long userId) {
        Driver driver = driverMapper.selectByUserId(String.valueOf(userId));
        if (driver == null) {
            return null;
        }
        DriverVO vo = convertToVO(driver);
        vo.setCommonVehicles(getCommonVehicles(driver.getId()));
        return vo;
    }

    @Override
    public List<DriverVO> getList(String driverName, Integer status) {
        LambdaQueryWrapper<Driver> wrapper = new LambdaQueryWrapper<>();
        
        if (driverName != null && !driverName.isEmpty()) {
            wrapper.like(Driver::getDriverName, driverName);
        }
        if (status != null) {
            wrapper.eq(Driver::getStatus, status);
        }
        
        wrapper.eq(Driver::getDeleted, 0);
        wrapper.orderByDesc(Driver::getCreateTime);
        
        List<Driver> list = driverMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStatus(Long driverId, Integer status) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) {
            throw new RuntimeException("司机信息不存在");
        }
        driver.setStatus(status);
        driver.setUpdateTime(LocalDateTime.now());
        driverMapper.updateById(driver);
        log.info("更新司机状态：司机ID={}, 状态={}", driverId, status);
    }

    @Override
    public List<DriverVO> getAvailableDrivers() {
        LambdaQueryWrapper<Driver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Driver::getStatus, 1);
        wrapper.eq(Driver::getDeleted, 0);
        wrapper.orderByAsc(Driver::getCreateTime);
        
        List<Driver> list = driverMapper.selectList(wrapper);
        return list.stream()
            .filter(d -> hasRole(d.getUserId(), "ROLE_DRIVER"))
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    public List<DriverVO> getAvailableRepairmen() {
        LambdaQueryWrapper<Driver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Driver::getStatus, 1);
        wrapper.eq(Driver::getDeleted, 0);
        wrapper.orderByAsc(Driver::getCreateTime);
        
        List<Driver> list = driverMapper.selectList(wrapper);
        return list.stream()
            .filter(d -> hasRole(d.getUserId(), "ROLE_REPAIRMAN"))
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    public List<DriverVO> getAvailableSafetyOfficers() {
        LambdaQueryWrapper<Driver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Driver::getStatus, 1);
        wrapper.eq(Driver::getDeleted, 0);
        wrapper.orderByAsc(Driver::getCreateTime);
        
        List<Driver> list = driverMapper.selectList(wrapper);
        return list.stream()
            .filter(d -> hasRole(d.getUserId(), "ROLE_SAFETY_OFFICER"))
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }
    
    private boolean hasRole(Long userId, String roleCode) {
        return driverMapper.hasRole(userId, roleCode);
    }

    @Override
    public DriverVO selectBestDriver(Long vehicleId, String scheduledTime) {
        List<DriverVO> availableDrivers = getAvailableDrivers();
        
        if (availableDrivers.isEmpty()) {
            log.warn("没有可用司机");
            return null;
        }
        
        if (vehicleId != null) {
            for (DriverVO driver : availableDrivers) {
                List<DriverVehicleVO> commonVehicles = getCommonVehicles(driver.getId());
                for (DriverVehicleVO dv : commonVehicles) {
                    if (vehicleId.equals(dv.getVehicleId())) {
                        driver.setScore((int) calculateScore(driver));
                        return driver;
                    }
                }
            }
        }
        
        DriverVO bestDriver = availableDrivers.stream()
                .peek(d -> d.setScore((int) calculateScore(d)))
                .max((d1, d2) -> Double.compare(d1.getScore(), d2.getScore()))
                .orElse(availableDrivers.get(0));
        
        return bestDriver;
    }

    @Override
    @Transactional
    public void addCommonVehicle(Long driverId, Long vehicleId) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) {
            throw new RuntimeException("司机信息不存在");
        }
        
        DriverVehicle existing = driverVehicleMapper.selectByDriverAndVehicle(driverId, vehicleId);
        if (existing != null) {
            log.info("车辆已是司机的常用车辆：司机ID={}, 车辆ID={}", driverId, vehicleId);
            return;
        }
        
        List<DriverVehicle> existingList = driverVehicleMapper.selectByDriverId(driverId);
        
        DriverVehicle dv = new DriverVehicle();
        dv.setDriverId(driverId);
        dv.setVehicleId(vehicleId);
        dv.setUseCount(0);
        dv.setIsDefault(existingList.isEmpty() ? 1 : 0);
        dv.setCreateTime(LocalDateTime.now());
        dv.setUpdateTime(LocalDateTime.now());
        dv.setDeleted(0);
        
        driverVehicleMapper.insert(dv);
        log.info("添加常用车辆：司机ID={}, 车辆ID={}", driverId, vehicleId);
    }

    @Override
    @Transactional
    public void removeCommonVehicle(Long driverId, Long vehicleId) {
        DriverVehicle dv = driverVehicleMapper.selectByDriverAndVehicle(driverId, vehicleId);
        if (dv == null) {
            throw new RuntimeException("常用车辆关系不存在");
        }
        
        boolean wasDefault = dv.getIsDefault() != null && dv.getIsDefault() == 1;
        
        dv.setDeleted(1);
        dv.setUpdateTime(LocalDateTime.now());
        driverVehicleMapper.updateById(dv);
        
        if (wasDefault) {
            List<DriverVehicle> remaining = driverVehicleMapper.selectByDriverId(driverId);
            if (!remaining.isEmpty()) {
                DriverVehicle newDefault = remaining.get(0);
                newDefault.setIsDefault(1);
                newDefault.setUpdateTime(LocalDateTime.now());
                driverVehicleMapper.updateById(newDefault);
                log.info("自动设置新的默认车辆：司机ID={}, 车辆ID={}", driverId, newDefault.getVehicleId());
            }
        }
        
        log.info("移除常用车辆：司机ID={}, 车辆ID={}", driverId, vehicleId);
    }

    @Override
    @Transactional
    public void setDefaultVehicle(Long driverId, Long vehicleId) {
        DriverVehicle dv = driverVehicleMapper.selectByDriverAndVehicle(driverId, vehicleId);
        if (dv == null) {
            throw new RuntimeException("常用车辆关系不存在");
        }
        
        driverVehicleMapper.clearDefaultByDriverId(driverId);
        
        dv.setIsDefault(1);
        dv.setUpdateTime(LocalDateTime.now());
        driverVehicleMapper.updateById(dv);
        
        log.info("设置默认车辆：司机ID={}, 车辆ID={}", driverId, vehicleId);
    }

    @Override
    public List<DriverVehicleVO> getCommonVehicles(Long driverId) {
        List<DriverVehicle> list = driverVehicleMapper.selectByDriverId(driverId);
        
        return list.stream().map(dv -> {
            DriverVehicleVO vo = new DriverVehicleVO();
            vo.setId(dv.getId());
            vo.setDriverId(dv.getDriverId());
            vo.setVehicleId(dv.getVehicleId());
            vo.setUseCount(dv.getUseCount());
            vo.setLastUseTime(dv.getLastUseTime());
            vo.setIsDefault(dv.getIsDefault() != null && dv.getIsDefault() == 1);
            
            try {
                String url = "http://mineguard-service-vehicle/api/vehicle/" + dv.getVehicleId();
                @SuppressWarnings("unchecked")
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                if (response != null && response.get("data") != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> vehicleData = (Map<String, Object>) response.get("data");
                    vo.setVehicleNo((String) vehicleData.get("vehicleNo"));
                    vo.setVehicleType((String) vehicleData.get("vehicleType"));
                }
            } catch (Exception e) {
                log.warn("获取车辆信息失败：车辆ID={}", dv.getVehicleId());
            }
            
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void incrementVehicleUseCount(Long driverId, Long vehicleId) {
        driverVehicleMapper.incrementUseCount(driverId, vehicleId);
        log.info("增加车辆使用次数：司机ID={}, 车辆ID={}", driverId, vehicleId);
    }

    private DriverVO convertToVO(Driver entity) {
        DriverVO vo = new DriverVO();
        BeanUtils.copyProperties(entity, vo);
        
        vo.setGenderName(getGenderName(entity.getGender()));
        vo.setStatusName(getStatusName(entity.getStatus()));
        vo.setDrivingYears(entity.getDrivingYears());
        vo.setScore(entity.getScore() != null ? entity.getScore() : 10);
        
        if (entity.getIdCard() != null && entity.getIdCard().length() >= 15) {
            String masked = entity.getIdCard().substring(0, 6) + "****" + 
                           entity.getIdCard().substring(entity.getIdCard().length() - 4);
            vo.setIdCardMasked(masked);
        }
        
        return vo;
    }

    private String getGenderName(Integer gender) {
        if (gender == null) return "未知";
        switch (gender) {
            case 1: return "男";
            case 2: return "女";
            default: return "未知";
        }
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "离职";
            case 1: return "在职";
            case 2: return "休假";
            case 3: return "停职";
            default: return "未知";
        }
    }

    private double calculateScore(DriverVO driver) {
        double score = 10.0;
        
        if (driver.getDrivingYears() != null && driver.getDrivingYears() > 5) {
            score += driver.getDrivingYears() * 0.5;
        }
        
        if (driver.getStatus() != null && driver.getStatus() == 1) {
            score += 5;
        }
        
        if (driver.getCommonVehicles() != null && !driver.getCommonVehicles().isEmpty()) {
            score += driver.getCommonVehicles().size() * 2;
        }
        
        return Math.min(score, 100);
    }
}
