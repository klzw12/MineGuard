package com.klzw.service.warning.service.impl;

import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.common.core.client.TripClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.client.UserClient;
import com.klzw.common.core.client.MessageClient;
import com.klzw.service.warning.constant.WarningResultCode;
import com.klzw.service.warning.dto.WarningHandleDTO;
import com.klzw.service.warning.dto.WarningRecordDTO;
import com.klzw.service.warning.dto.WarningTrackDTO;
import com.klzw.service.warning.entity.WarningRecord;
import com.klzw.service.warning.entity.WarningRule;
import com.klzw.service.warning.enums.WarningLevelEnum;
import com.klzw.service.warning.enums.WarningRecordStatusEnum;
import com.klzw.service.warning.enums.WarningTypeEnum;
import com.klzw.service.warning.exception.WarningException;
import com.klzw.service.warning.mapper.WarningRecordMapper;
import com.klzw.service.warning.mapper.WarningRuleMapper;
import com.klzw.service.warning.processor.EventTriggerProcessor;
import com.klzw.service.warning.processor.WarningTriggerProcessor;
import com.klzw.service.warning.service.WarningRecordService;
import com.klzw.service.warning.vo.WarningRecordVO;
import com.klzw.common.redis.service.RedisCacheService;

// Mapper 依赖已添加，用于查询关联信息（如预警规则名称等）
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarningRecordServiceImpl implements WarningRecordService {

    private final WarningRecordMapper warningRecordMapper;
    private final WarningRuleMapper warningRuleMapper;
    private final EventTriggerProcessor eventTriggerProcessor;
    private final WarningTriggerProcessor warningTriggerProcessor;
    private final RedisCacheService redisCacheService;
    private final TripClient tripClient;
    private final VehicleClient vehicleClient;
    private final UserClient userClient;
    private final MessageClient messageClient;

    @Override
    public PageResult<WarningRecordVO> page(PageRequest pageRequest) {
        Page<WarningRecord> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        LambdaQueryWrapper<WarningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(WarningRecord::getWarningTime);

        Page<WarningRecord> result = warningRecordMapper.selectPage(page, wrapper);

        List<WarningRecordVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), pageRequest.getPage(), pageRequest.getSize(), voList);
    }
    
    @Override
    public PageResult<WarningRecordVO> pageWithFilters(PageRequest pageRequest, Integer status, Integer warningLevel, Integer warningType, Long vehicleId, Long driverId) {
        Page<WarningRecord> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        LambdaQueryWrapper<WarningRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(WarningRecord::getStatus, status);
        }
        if (warningLevel != null) {
            wrapper.eq(WarningRecord::getWarningLevel, warningLevel);
        }
        if (warningType != null) {
            wrapper.eq(WarningRecord::getWarningType, warningType);
        }
        if (vehicleId != null) {
            wrapper.eq(WarningRecord::getVehicleId, vehicleId);
        }
        if (driverId != null) {
            wrapper.eq(WarningRecord::getDriverId, driverId);
        }
        
        wrapper.orderByDesc(WarningRecord::getWarningTime);

        Page<WarningRecord> result = warningRecordMapper.selectPage(page, wrapper);

        List<WarningRecordVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), pageRequest.getPage(), pageRequest.getSize(), voList);
    }

    @Override
    public WarningRecordVO getById(Long id) {
        WarningRecord record = warningRecordMapper.selectById(id);
        if (record == null) {
            throw new WarningException(WarningResultCode.WARNING_RECORD_NOT_FOUND, "预警记录不存在：" + id);
        }
        return convertToVO(record);
    }
    
    @Override
    public List<WarningRecordVO> getByTripId(Long tripId) {
        LambdaQueryWrapper<WarningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningRecord::getTripId, tripId)
                .orderByDesc(WarningRecord::getWarningTime);
        List<WarningRecord> records = warningRecordMapper.selectList(wrapper);
        return records.stream()
                .map(this::convertToVO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WarningRecord processEventTrigger(WarningTrackDTO tripTrack, String eventType) {
        WarningRecord warningRecord = eventTriggerProcessor.processEventTrigger(tripTrack, eventType);
        warningRecordMapper.insert(warningRecord);
        pushWarningNotification(warningRecord);
        return warningRecord;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WarningRecord processWarningTrack(WarningTrackDTO track) {
        WarningRecord warningRecord = warningTriggerProcessor.processTrack(track);
        if (warningRecord != null) {
            warningRecordMapper.insert(warningRecord);
            pushWarningNotification(warningRecord);
            
            // 根据预警级别处理行程状态
            handleTripStatusByWarningLevel(warningRecord);
        }
        return warningRecord;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWarning(WarningRecordDTO dto) {
        WarningRecord record = new WarningRecord();
        BeanUtils.copyProperties(dto, record);
        record.setWarningNo(generateWarningNo());
        record.setStatus(WarningRecordStatusEnum.PENDING.getCode());
        record.setWarningTime(LocalDateTime.now());

        warningRecordMapper.insert(record);
        log.info("创建预警记录成功，编号：{}", record.getWarningNo());

        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWarning(Long id, WarningHandleDTO dto) {
        WarningRecord record = warningRecordMapper.selectById(id);
        if (record == null) {
            throw new WarningException(WarningResultCode.WARNING_RECORD_NOT_FOUND, "预警记录不存在：" + id);
        }
        
        if (record.getStatus() == null || record.getStatus() == WarningRecordStatusEnum.RESOLVED.getCode()) {
            throw new WarningException(WarningResultCode.WARNING_ALREADY_HANDLED);
        }
        if (record.getStatus() == WarningRecordStatusEnum.IGNORED.getCode()) {
            throw new WarningException(WarningResultCode.WARNING_ALREADY_CLOSED);
        }

        record.setStatus(dto.getStatus() != null ? dto.getStatus() : WarningRecordStatusEnum.RESOLVED.getCode());
        record.setHandleResult(dto.getHandleResult());
        record.setHandleTime(LocalDateTime.now());
        warningRecordMapper.updateById(record);
        log.info("处理预警记录成功，ID：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ignoreWarning(Long id) {
        WarningRecord record = warningRecordMapper.selectById(id);
        if (record == null) {
            throw new WarningException(WarningResultCode.WARNING_RECORD_NOT_FOUND, "预警记录不存在：" + id);
        }
        
        if (record.getStatus() == null || record.getStatus() == WarningRecordStatusEnum.RESOLVED.getCode()) {
            throw new WarningException(WarningResultCode.WARNING_ALREADY_HANDLED);
        }
        if (record.getStatus() == WarningRecordStatusEnum.IGNORED.getCode()) {
            throw new WarningException(WarningResultCode.WARNING_ALREADY_CLOSED);
        }

        record.setStatus(WarningRecordStatusEnum.IGNORED.getCode());
        record.setHandleTime(LocalDateTime.now());
        warningRecordMapper.updateById(record);
        log.info("忽略预警记录成功， ID：{}", id);
    }

    @Override
    public void pushWarningNotification(WarningRecord warningRecord) {
        Integer level = warningRecord.getWarningLevel();
        Integer warningType = warningRecord.getWarningType();
        Long driverId = warningRecord.getDriverId();
        String title = "预警通知";
        String content = warningRecord.getWarningContent();

        if (level == null) {
            return;
        }

        if (level.equals(WarningLevelEnum.HIGH.getCode())) {
            log.error("【高危预警】立即推送全体安全员、维修员！预警内容： {}", content);
            messageClient.sendMessageByRole("ROLE_SAFETY_OFFICER", title, content, "WARNING");
            messageClient.sendMessageByRole("ROLE_REPAIRMAN", title, content, "WARNING");
            messageClient.sendMessage(1L, title, content, "WARNING", "");
        } else if (level.equals(WarningLevelEnum.MEDIUM.getCode())) {
            log.warn("【中危预警】推送给维修员处理! 预警内容： {}", content);
            messageClient.sendMessageByRole("ROLE_REPAIRMAN", title, content, "WARNING");
            messageClient.sendMessage(1L, title, content, "WARNING", "");
        } else if (level.equals(WarningLevelEnum.LOW.getCode())) {
            log.info("【低危预警】推送给相关人员! 预警内容: {}", content);
        }
        
        if (warningType != null && driverId != null) {
            if (warningType.equals(WarningTypeEnum.SPEED_ABNORMAL.getCode()) 
                    || warningType.equals(WarningTypeEnum.FATIGUE_DRIVING.getCode())) {
                log.info("【驾驶行为预警】推送给司机！预警内容：{}", content);
                messageClient.sendMessage(driverId, title, content, "WARNING", "");
            } else if (warningType.equals(WarningTypeEnum.ROUTE_DEVIATION.getCode())
                    || warningType.equals(WarningTypeEnum.LONG_STAY.getCode())) {
                log.info("【路线异常】推送给司机！预警内容：{}", content);
                messageClient.sendMessage(driverId, title, content, "WARNING", "");
            }
        }
    }

    @Override
    public Map<String, Object> getWarningStatistics(String startTime, String endTime) {
        Map<String, Object> statistics = new HashMap<>();

        LambdaQueryWrapper<WarningRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
            wrapper.between(WarningRecord::getWarningTime, startTime, endTime);
        }

        Long totalCount = warningRecordMapper.selectCount(wrapper);
        statistics.put("totalCount", totalCount);

        LambdaQueryWrapper<WarningRecord> lowWrapper = new LambdaQueryWrapper<>();
        lowWrapper.eq(WarningRecord::getWarningLevel, WarningLevelEnum.LOW.getCode());
        if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
            lowWrapper.between(WarningRecord::getWarningTime, startTime, endTime);
        }
        statistics.put("lowLevelCount", warningRecordMapper.selectCount(lowWrapper));

        LambdaQueryWrapper<WarningRecord> mediumWrapper = new LambdaQueryWrapper<>();
        mediumWrapper.eq(WarningRecord::getWarningLevel, WarningLevelEnum.MEDIUM.getCode());
        if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
            mediumWrapper.between(WarningRecord::getWarningTime, startTime, endTime);
        }
        statistics.put("mediumLevelCount", warningRecordMapper.selectCount(mediumWrapper));

        LambdaQueryWrapper<WarningRecord> highWrapper = new LambdaQueryWrapper<>();
        highWrapper.eq(WarningRecord::getWarningLevel, WarningLevelEnum.HIGH.getCode());
        if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
            highWrapper.between(WarningRecord::getWarningTime, startTime, endTime);
        }
        statistics.put("highLevelCount", warningRecordMapper.selectCount(highWrapper));

        LambdaQueryWrapper<WarningRecord> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(WarningRecord::getStatus, WarningRecordStatusEnum.PENDING.getCode());
        if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
            pendingWrapper.between(WarningRecord::getWarningTime, startTime, endTime);
        }
        statistics.put("pendingCount", warningRecordMapper.selectCount(pendingWrapper));

        LambdaQueryWrapper<WarningRecord> handledWrapper = new LambdaQueryWrapper<>();
        handledWrapper.in(WarningRecord::getStatus, WarningRecordStatusEnum.RESOLVED.getCode(), WarningRecordStatusEnum.IGNORED.getCode());
        if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
            handledWrapper.between(WarningRecord::getWarningTime, startTime, endTime);
        }
        statistics.put("handledCount", warningRecordMapper.selectCount(handledWrapper));

        return statistics;
    }

    @Override
    public List<Map<String, Object>> getWarningTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(formatter));

            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            LambdaQueryWrapper<WarningRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(WarningRecord::getWarningTime, startOfDay, endOfDay);
            dayData.put("count", warningRecordMapper.selectCount(wrapper));
            trend.add(dayData);
        }

        return trend;
    }

    @Override
    public Map<String, Object> getWarningTypeStatistics(String startTime, String endTime) {
        Map<String, Object> statistics = new HashMap<>();

        for (WarningTypeEnum type : WarningTypeEnum.values()) {
                LambdaQueryWrapper<WarningRecord> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(WarningRecord::getWarningType, type.getCode());
                if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
                    wrapper.between(WarningRecord::getWarningTime, startTime, endTime);
                }
                statistics.put(type.getName(), warningRecordMapper.selectCount(wrapper));
            }

        return statistics;
    }

    @Override
    public Map<String, Object> getWarningLevelStatistics(String startTime, String endTime) {
        Map<String, Object> statistics = new HashMap<>();

        for (WarningLevelEnum level : WarningLevelEnum.values()) {
                LambdaQueryWrapper<WarningRecord> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(WarningRecord::getWarningLevel, level.getCode());
                if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
                    wrapper.between(WarningRecord::getWarningTime, startTime, endTime);
                }
                statistics.put(level.getName(), warningRecordMapper.selectCount(wrapper));
            }

        return statistics;
    }

    private WarningRecordVO convertToVO(WarningRecord record) {
        WarningRecordVO vo = new WarningRecordVO();
        BeanUtils.copyProperties(record, vo);

        WarningTypeEnum typeEnum = WarningTypeEnum.getByCode(record.getWarningType());
        if (typeEnum != null) {
            vo.setWarningTypeName(typeEnum.getName());
        }

        WarningLevelEnum levelEnum = WarningLevelEnum.getByCode(record.getWarningLevel());
        if (levelEnum != null) {
            vo.setWarningLevelName(levelEnum.getName());
        }

        WarningRecordStatusEnum statusEnum = WarningRecordStatusEnum.getByCode(record.getStatus());
        if (statusEnum != null) {
            vo.setStatusName(statusEnum.getName());
        }

        if (record.getRuleId() != null) {
            WarningRule rule = warningRuleMapper.selectById(record.getRuleId());
            if (rule != null) {
                vo.setRuleName(rule.getRuleName());
            }
        }
        
        if (record.getVehicleId() != null) {
            String vehicleInfo = getVehicleNo(record.getVehicleId());
            vo.setVehicleNo(vehicleInfo);
        }
        
        if (record.getDriverId() != null) {
            String driverInfo = getDriverName(record.getDriverId());
            vo.setDriverName(driverInfo);
        }
        
        if (record.getTripId() != null) {
            String tripInfo = getTripNo(record.getTripId());
            vo.setTripNo(tripInfo);
        }
        
        if (record.getHandlerId() != null) {
            String handlerInfo = getHandlerName(record.getHandlerId());
            vo.setHandlerName(handlerInfo);
        }

        return vo;
    }
    
    private String getVehicleNo(Long vehicleId) {
        String key = "vehicle:no:" + vehicleId;
        String cached = redisCacheService.get(key);
        if (cached != null) {
            return cached;
        }
        
        String vehicleNo = "未知车辆";
        try {
            var vehicleInfoResult = vehicleClient.getById(vehicleId);
            if (vehicleInfoResult != null && vehicleInfoResult.getCode() == 200 && vehicleInfoResult.getData() != null) {
                var vehicleInfo = vehicleInfoResult.getData();
                vehicleNo = (String) vehicleInfo.get("vehicleNo");
                if (vehicleNo != null) {
                    redisCacheService.set(key, vehicleNo, 24, TimeUnit.HOURS);
                }
            }
        } catch (Exception e) {
            log.warn("查询车辆信息失败：vehicleId={}", vehicleId);
        }
        
        return vehicleNo;
    }
    
    private String getDriverName(Long driverId) {
        String key = "driver:name:" + driverId;
        String cached = redisCacheService.get(key);
        if (cached != null) {
            return cached;
        }
        
        String driverName = "未知司机";
        try {
            var userInfo = userClient.getUserById(driverId);
            if (userInfo != null && userInfo.getData() != null) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> userData = (java.util.Map<String, Object>) userInfo.getData();
                if (userData.get("realName") != null) {
                    driverName = (String) userData.get("realName");
                    redisCacheService.set(key, driverName, 24, TimeUnit.HOURS);
                }
            }
        } catch (Exception e) {
            log.warn("查询司机信息失败：driverId={}", driverId);
        }
        
        return driverName;
    }
    
    private String getTripNo(Long tripId) {
        String key = "trip:no:" + tripId;
        String cached = redisCacheService.get(key);
        if (cached != null) {
            return cached;
        }
        
        String tripNo = "未知行程";
        try {
            var tripInfoResult = tripClient.getTripById(tripId).block();
            if (tripInfoResult != null && tripInfoResult.getCode() == 200 && tripInfoResult.getData() != null) {
                var tripInfo = tripInfoResult.getData();
                if (tripInfo.getTripNo() != null) {
                    tripNo = tripInfo.getTripNo();
                    redisCacheService.set(key, tripNo, 24, TimeUnit.HOURS);
                }
            }
        } catch (Exception e) {
            log.warn("查询行程信息失败：tripId={}", tripId);
        }
        
        return tripNo;
    }
    
    private String getHandlerName(Long handlerId) {
        String key = "handler:name:" + handlerId;
        String cached = redisCacheService.get(key);
        if (cached != null) {
            return cached;
        }
        
        String handlerName = "未知处理人";
        try {
            var userInfo = userClient.getUserById(handlerId);
            if (userInfo != null && userInfo.getData() != null) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> userData = (java.util.Map<String, Object>) userInfo.getData();
                if (userData.get("realName") != null) {
                    handlerName = (String) userData.get("realName");
                    redisCacheService.set(key, handlerName, 24, TimeUnit.HOURS);
                }
            }
        } catch (Exception e) {
            log.warn("查询处理人信息失败：handlerId={}", handlerId);
        }
        
        return handlerName;
    }

    private String generateWarningNo() {
        return "WARN" + System.currentTimeMillis() + new Random().nextInt(1000);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWarningsByVehicleId(Long vehicleId, Integer status, String handleResult) {
        if (vehicleId == null) {
            log.warn("车辆ID为空，无法处理预警记录");
            return;
        }
        
        LambdaQueryWrapper<WarningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningRecord::getVehicleId, vehicleId)
               .eq(WarningRecord::getStatus, WarningRecordStatusEnum.PENDING.getCode());
        
        List<WarningRecord> pendingRecords = warningRecordMapper.selectList(wrapper);
        
        if (pendingRecords.isEmpty()) {
            log.info("车辆{}没有待处理的预警记录", vehicleId);
            return;
        }
        
        int updateCount = 0;
        for (WarningRecord record : pendingRecords) {
            record.setStatus(status != null ? status : WarningRecordStatusEnum.RESOLVED.getCode());
            record.setHandleResult(handleResult != null ? handleResult : "维修完成，自动处理");
            record.setHandleTime(LocalDateTime.now());
            warningRecordMapper.updateById(record);
            updateCount++;
        }
        
        log.info("已处理车辆{}的{}条待处理预警记录", vehicleId, updateCount);
    }
    
    /**
     * 根据预警级别处理行程状态
     * - 低危预警：不改 trip 状态
     * - 中危预警（车辆故障）：暂停行程
     * - 高危预警（坠崖）：结束行程，持久化到 MongoDB，删除 Redis 数据
     */
    private void handleTripStatusByWarningLevel(WarningRecord warningRecord) {
        if (warningRecord.getTripId() == null) {
            log.warn("预警记录没有关联行程：warningId={}", warningRecord.getId());
            return;
        }
        
        Integer warningLevel = warningRecord.getWarningLevel();

        Long tripId = warningRecord.getTripId();
        
        try {
            if (warningLevel != null && WarningLevelEnum.MEDIUM.getCode() == warningLevel) {
                // 中危预警：暂停行程
                log.info("中危预警，暂停行程：warningId={}, tripId={}", warningRecord.getId(), tripId);
                tripClient.pauseTrip(tripId).block();
                
            } else if (warningLevel != null && WarningLevelEnum.HIGH.getCode() == warningLevel) {
                // 高危预警：结束行程，持久化到 MongoDB，删除 Redis 数据
                log.info("高危预警，结束行程：warningId={}, tripId={}", warningRecord.getId(), tripId);
                tripClient.endTrip(tripId).block();
                
            } else {
                // 低危预警：不改变行程状态
                log.debug("低危预警，不改变行程状态：warningId={}, tripId={}", warningRecord.getId(), tripId);
            }
        } catch (Exception e) {
            log.error("根据预警级别处理行程状态失败：warningId={}, tripId={}", warningRecord.getId(), tripId, e);
        }
    }
}
