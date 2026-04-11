package com.klzw.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.auth.enums.RoleEnum;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.service.user.entity.Driver;
import com.klzw.service.user.entity.DriverVehicle;
import com.klzw.service.user.entity.Repairman;
import com.klzw.service.user.entity.SafetyOfficer;
import com.klzw.service.user.enums.DriverStatusEnum;
import com.klzw.service.user.mapper.DriverMapper;
import com.klzw.service.user.mapper.DriverVehicleMapper;
import com.klzw.service.user.mapper.RepairmanMapper;
import com.klzw.service.user.mapper.SafetyOfficerMapper;
import com.klzw.service.user.mapper.UserAttendanceMapper;
import com.klzw.service.user.service.DriverService;
import com.klzw.service.user.vo.DriverVehicleVO;
import com.klzw.service.user.vo.DriverVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverMapper driverMapper;
    private final DriverVehicleMapper driverVehicleMapper;
    private final VehicleClient vehicleClient;
    private final RepairmanMapper repairmanMapper;
    private final SafetyOfficerMapper safetyOfficerMapper;
    private final UserAttendanceMapper userAttendanceMapper;

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
        wrapper.eq(Driver::getStatus, DriverStatusEnum.EMPLOYED.getValue());
        wrapper.eq(Driver::getDeleted, 0);
        wrapper.orderByAsc(Driver::getCreateTime);
        
        List<Driver> list = driverMapper.selectList(wrapper);
        return list.stream()
            .filter(d -> hasRole(d.getUserId(), RoleEnum.DRIVER.getValue()))
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    public List<DriverVO> getAvailableRepairmen() {
        LambdaQueryWrapper<Repairman> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Repairman::getStatus, 1);
        wrapper.eq(Repairman::getDeleted, 0);
        wrapper.orderByAsc(Repairman::getCreateTime);
        
        List<Repairman> list = repairmanMapper.selectList(wrapper);
        return list.stream()
            .filter(r -> hasRole(r.getUserId(), RoleEnum.REPAIRMAN.getValue()))
            .map(this::convertRepairmanToVO)
            .collect(Collectors.toList());
    }

    @Override
    public List<DriverVO> getAvailableSafetyOfficers() {
        LambdaQueryWrapper<SafetyOfficer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SafetyOfficer::getStatus, 1);
        wrapper.eq(SafetyOfficer::getDeleted, 0);
        wrapper.orderByAsc(SafetyOfficer::getCreateTime);
        
        List<SafetyOfficer> list = safetyOfficerMapper.selectList(wrapper);
        return list.stream()
            .filter(s -> hasRole(s.getUserId(), RoleEnum.SAFETY_OFFICER.getValue()))
            .map(this::convertSafetyOfficerToVO)
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
        
        // 如果指定了计划时间，过滤掉该时间段已有任务的司机
        if (scheduledTime != null && !scheduledTime.isEmpty()) {
            try {
                LocalDate targetDate = LocalDate.parse(scheduledTime);
                LocalDateTime startTime = targetDate.atStartOfDay();
                LocalDateTime endTime = targetDate.atTime(23, 59, 59);
                
                List<Long> busyDriverIds = driverMapper.findBusyDriverIds(startTime, endTime);
                if (!busyDriverIds.isEmpty()) {
                    log.info("时间段 {} 已有任务的司机ID: {}", scheduledTime, busyDriverIds);
                    int beforeSize = availableDrivers.size();
                    availableDrivers = availableDrivers.stream()
                        .filter(d -> !busyDriverIds.contains(d.getId()))
                        .collect(java.util.stream.Collectors.toList());
                    
                    if (availableDrivers.isEmpty()) {
                        log.warn("时间段 {} 所有司机都有任务，放宽条件返回所有可用司机", scheduledTime);
                        availableDrivers = getAvailableDrivers();
                    } else {
                        log.info("时间段 {} 过滤后剩余 {} 个可用司机（原 {} 个）", 
                            scheduledTime, availableDrivers.size(), beforeSize);
                    }
                }
            } catch (java.time.format.DateTimeParseException e) {
                log.error("计划时间格式错误: {}, 期望格式: yyyy-MM-dd", scheduledTime);
                throw new com.klzw.common.core.exception.BaseException(
                    com.klzw.common.core.enums.ResultCodeEnum.PARAM_ERROR.getCode(),
                    "计划时间格式错误，期望格式: yyyy-MM-dd");
            } catch (Exception e) {
                log.error("解析计划时间失败: {}", scheduledTime, e);
                throw new com.klzw.common.core.exception.BaseException(
                    com.klzw.common.core.enums.ResultCodeEnum.PARAM_ERROR.getCode(),
                    "计划时间解析失败: " + e.getMessage());
            }
        }
        
        if (vehicleId != null) {
            for (DriverVO driver : availableDrivers) {
                List<DriverVehicleVO> commonVehicles = getCommonVehicles(driver.getId());
                for (DriverVehicleVO dv : commonVehicles) {
                    if (vehicleId.equals(dv.getVehicleId())) {
                        driver.setScore((int) calculateScore(driver));
                        log.info("为车辆 {} 找到匹配常用车司机: {}", vehicleId, driver.getDriverName());
                        return driver;
                    }
                }
            }
        }

        return availableDrivers.stream()
                .peek(d -> d.setScore((int) calculateScore(d)))
                .max((d1, d2) -> Double.compare(d1.getScore(), d2.getScore()))
                .orElse(availableDrivers.getFirst());
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
                DriverVehicle newDefault = remaining.getFirst();
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
                var result = vehicleClient.getById(dv.getVehicleId());
                if (result != null && result.getData() != null) {
                    vo.setVehicleNo(result.getData().getVehicleNo());
                }
            } catch (Exception e) {
                log.warn("获取车辆信息失败：车辆 ID={}", dv.getVehicleId());
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

    @Override
    public List<Long> getDriverIds() {
        return driverMapper.selectList(
            new LambdaQueryWrapper<Driver>()
                .eq(Driver::getDeleted, 0)
                .eq(Driver::getStatus, 1)
        ).stream().map(Driver::getId).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateBelongingTeam(Long driverId, String belongingTeam) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) {
            throw new RuntimeException("司机信息不存在");
        }
        driver.setBelongingTeam(belongingTeam);
        driver.setUpdateTime(LocalDateTime.now());
        driverMapper.updateById(driver);
        log.info("更新司机班组：司机ID={}, 班组={}", driverId, belongingTeam);
    }

    @Override
    @Transactional
    public void updateBelongingTeamByUserId(Long userId, String belongingTeam) {
        Driver driver = driverMapper.selectByUserId(String.valueOf(userId));
        if (driver == null) {
            throw new RuntimeException("司机信息不存在");
        }
        updateBelongingTeam(driver.getId(), belongingTeam);
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
    
    private DriverVO convertRepairmanToVO(Repairman entity) {
        DriverVO vo = new DriverVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setDriverName(entity.getRepairmanName());
        vo.setGender(entity.getGender());
        vo.setGenderName(getGenderName(entity.getGender()));
        vo.setStatus(entity.getStatus());
        vo.setStatusName(getStatusName(entity.getStatus()));
        vo.setIdCardFrontUrl(entity.getIdCardFrontUrl());
        vo.setIdCardBackUrl(entity.getIdCardBackUrl());
        vo.setScore(10);
        
        if (entity.getIdCard() != null && entity.getIdCard().length() >= 15) {
            String masked = entity.getIdCard().substring(0, 6) + "****" + 
                           entity.getIdCard().substring(entity.getIdCard().length() - 4);
            vo.setIdCardMasked(masked);
        }
        
        return vo;
    }
    
    private DriverVO convertSafetyOfficerToVO(SafetyOfficer entity) {
        DriverVO vo = new DriverVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setDriverName(entity.getOfficerName());
        vo.setGender(entity.getGender());
        vo.setGenderName(getGenderName(entity.getGender()));
        vo.setStatus(entity.getStatus());
        vo.setStatusName(getStatusName(entity.getStatus()));
        vo.setIdCardFrontUrl(entity.getIdCardFrontUrl());
        vo.setIdCardBackUrl(entity.getIdCardBackUrl());
        vo.setScore(10);
        
        if (entity.getIdCard() != null && entity.getIdCard().length() >= 15) {
            String masked = entity.getIdCard().substring(0, 6) + "****" + 
                           entity.getIdCard().substring(entity.getIdCard().length() - 4);
            vo.setIdCardMasked(masked);
        }
        
        return vo;
    }

    private String getGenderName(Integer gender) {
        if (gender == null) return "未知";
        return switch (gender) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "未知";
        };
    }

    @Override
    public void addCommonVehicleByUserId(Long userId, Long vehicleId) {
        Driver driver = driverMapper.selectByUserId(String.valueOf(userId));
        if (driver == null) {
            throw new RuntimeException("司机信息不存在");
        }
        addCommonVehicle(driver.getId(), vehicleId);
    }

    @Override
    public void removeCommonVehicleByUserId(Long userId, Long vehicleId) {
        Driver driver = driverMapper.selectByUserId(String.valueOf(userId));
        if (driver == null) {
            throw new RuntimeException("司机信息不存在");
        }
        removeCommonVehicle(driver.getId(), vehicleId);
    }

    @Override
    public void setDefaultVehicleByUserId(Long userId, Long vehicleId) {
        Driver driver = driverMapper.selectByUserId(String.valueOf(userId));
        if (driver == null) {
            throw new RuntimeException("司机信息不存在");
        }
        setDefaultVehicle(driver.getId(), vehicleId);
    }

    @Override
    public List<DriverVehicleVO> getCommonVehiclesByUserId(Long userId) {
        Driver driver = driverMapper.selectByUserId(String.valueOf(userId));
        if (driver == null) {
            throw new RuntimeException("司机信息不存在");
        }
        return getCommonVehicles(driver.getId());
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "离职";
            case 1 -> "在职";
            case 2 -> "休假";
            case 3 -> "停职";
            default -> "未知";
        };
    }

    private double calculateScore(DriverVO driver) {
        double score = 10.0;

        if (driver.getDrivingYears() != null && driver.getDrivingYears() > 0) {
            score += Math.min(driver.getDrivingYears() / 3.0, 5);
        }

        if (driver.getStatus() != null && driver.getStatus().equals(DriverStatusEnum.EMPLOYED.getValue())) {
            score += 5;
        }

        if (driver.getCommonVehicles() != null && !driver.getCommonVehicles().isEmpty()) {
            score += Math.min(driver.getCommonVehicles().size(), 5);
        }

        if (driver.getUserId() != null) {
            try {
                LocalDate now = LocalDate.now();
                LocalDate monthStart = now.minusDays(30);
                Integer normalDays = userAttendanceMapper.countNormalDays(driver.getUserId(), monthStart, now);
                if (normalDays != null && normalDays > 0) {
                    score += Math.min(normalDays * 0.5, 10);
                }
                Integer lateAndEarly = userAttendanceMapper.countLateAndEarlyLeaveDays(driver.getUserId(), monthStart, now);
                if (lateAndEarly != null && lateAndEarly > 0) {
                    score -= Math.min(lateAndEarly * 0.5, 5);
                }
            } catch (Exception e) {
                log.warn("查询司机出勤数据失败：driverId={}", driver.getId());
            }

            try {
                int completedTrips = driverMapper.countCompletedTrips(driver.getUserId());
                int totalTrips = driverMapper.countTotalTrips(driver.getUserId());
                if (totalTrips > 0) {
                    double completionRate = (double) completedTrips / totalTrips;
                    score += completionRate * 10;
                }
                score += Math.min(completedTrips * 0.3, 8);
            } catch (Exception e) {
                log.warn("查询司机行程数据失败：driverId={}", driver.getId());
            }
        }

        if (driver.getCommonVehicles() != null && !driver.getCommonVehicles().isEmpty()) {
            try {
                for (DriverVehicleVO dv : driver.getCommonVehicles()) {
                    if (dv.getVehicleId() != null && dv.getUseCount() != null && dv.getUseCount() > 3) {
                        var result = vehicleClient.getById(dv.getVehicleId());
                        if (result != null && result.getData() != null) {
                            var vInfo = result.getData();
                            if (vInfo.getFuelLevel() != null && vInfo.getFuelLevel() >= 70) {
                                score += 1;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("查询车辆状态失败：driverId={}", driver.getId());
            }
        }

        return Math.max(0, Math.min(score, 100));
    }
}
