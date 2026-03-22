package com.klzw.service.warning.service.impl;

import com.klzw.common.core.client.MessageClient;
import com.klzw.service.warning.dto.WarningTrackDTO;
import com.klzw.service.warning.entity.WarningRecord;
import com.klzw.service.warning.entity.WarningRule;
import com.klzw.service.warning.enums.WarningLevelEnum;
import com.klzw.service.warning.mapper.WarningRuleMapper;
import com.klzw.service.warning.processor.EventTriggerProcessor;
import com.klzw.service.warning.processor.WarningTriggerProcessor;
import com.klzw.service.warning.service.WarningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarningServiceImpl implements WarningService {

    private final EventTriggerProcessor eventTriggerProcessor;
    private final WarningTriggerProcessor warningTriggerProcessor;
    private final MessageClient messageClient;
    private final WarningRuleMapper warningRuleMapper;

    @Override
    public WarningRecord processEventTrigger(WarningTrackDTO tripTrack, String eventType) {
        WarningRecord warningRecord = eventTriggerProcessor.processEventTrigger(tripTrack, eventType);
        saveWarningRecord(warningRecord);
        pushWarningNotification(warningRecord);
        return warningRecord;
    }

    @Override
    public WarningRecord processWarningTrack(WarningTrackDTO track) {
        WarningRecord warningRecord = warningTriggerProcessor.processTrack(track);
        if (warningRecord != null) {
            saveWarningRecord(warningRecord);
            pushWarningNotification(warningRecord);
        }
        return warningRecord;
    }

    @Override
    public boolean saveWarningRecord(WarningRecord warningRecord) {
        log.info("保存预警记录：{}", warningRecord.getWarningNo());
        return true;
    }

    @Override
    public boolean updateWarningStatus(Long id, Integer status) {
        log.info("更新预警状态：ID={}, 状态={}", id, status);
        return true;
    }

    @Override
    public void pushWarningNotification(WarningRecord warningRecord) {
        if (warningRecord.getRuleId() == null) {
            pushByLevel(warningRecord);
            return;
        }
        
        WarningRule rule = warningRuleMapper.selectById(warningRecord.getRuleId());
        if (rule == null || rule.getPushRoles() == null || rule.getPushRoles().isEmpty()) {
            pushByLevel(warningRecord);
            return;
        }
        
        String title = "预警通知";
        String content = warningRecord.getWarningContent();
        Integer type = 1;
        
        String[] roles = rule.getPushRoles().split(",");
        for (String role : roles) {
            String trimmedRole = role.trim();
            if (!trimmedRole.isEmpty()) {
                log.info("推送预警消息：role={}, content={}", trimmedRole, content);
                messageClient.sendMessageByRole(trimmedRole, title, content, type);
            }
        }
    }
    
    private void pushByLevel(WarningRecord warningRecord) {
        Integer warningType = warningRecord.getWarningType();
        Integer level = warningRecord.getWarningLevel();
        
        String title = "预警通知";
        String content = warningRecord.getWarningContent();
        Integer type = 1;
        
        if (warningType == null) {
            if (level != null) {
                pushByLevelOnly(level, title, content, type);
            }
            return;
        }
        
        pushByType(warningType, level, title, content, type, warningRecord.getDriverId());
    }
    
    private void pushByLevelOnly(Integer level, String title, String content, Integer type) {
        if (level.equals(WarningLevelEnum.HIGH.getCode())) {
            log.error("【高危预警】立即推送全体安全员、维修员！预警内容：{}", content);
            messageClient.sendMessageByRole("ROLE_SAFETY_OFFICER", title, content, type);
            messageClient.sendMessageByRole("ROLE_REPAIRMAN", title, content, type);
        } else if (level.equals(WarningLevelEnum.MEDIUM.getCode())) {
            log.warn("【中危预警】推送给维修员处理！预警内容：{}", content);
            messageClient.sendMessageByRole("ROLE_REPAIRMAN", title, content, type);
        } else if (level.equals(WarningLevelEnum.LOW.getCode())) {
            log.info("【低危预警】推送给相关人员！预警内容：{}", content);
        }
    }
    
    private void pushByType(Integer warningType, Integer level, String title, String content, Integer type, Long driverId) {
        if (warningType.equals(WarningTypeEnum.VEHICLE_FAULT.getCode())) {
            log.info("【车辆故障】推送给维修员！预警内容：{}", content);
            messageClient.sendMessageByRole("ROLE_REPAIRMAN", title, content, type);
        } else if (warningType.equals(WarningTypeEnum.SPEED_ABNORMAL.getCode()) 
                || warningType.equals(WarningTypeEnum.FATIGUE_DRIVING.getCode())) {
            log.info("【驾驶行为预警】推送给司机！预警内容：{}", content);
            if (driverId != null) {
                messageClient.sendMessage(driverId, title, content, type);
            }
        } else if (warningType.equals(WarningTypeEnum.DANGER_ZONE.getCode()) 
                || warningType.equals(WarningTypeEnum.ABNORMAL_BEHAVIOR.getCode())) {
            log.info("【安全预警】推送给安全员和维修员！预警内容：{}", content);
            messageClient.sendMessageByRole("ROLE_SAFETY_OFFICER", title, content, type);
            messageClient.sendMessageByRole("ROLE_REPAIRMAN", title, content, type);
            if (driverId != null) {
                messageClient.sendMessage(driverId, title, content, type);
            }
        } else if (warningType.equals(WarningTypeEnum.ROUTE_DEVIATION.getCode())
                || warningType.equals(WarningTypeEnum.LONG_STAY.getCode())) {
            log.info("【路线异常】推送给维修员！预警内容：{}", content);
            messageClient.sendMessageByRole("ROLE_REPAIRMAN", title, content, type);
            if (driverId != null) {
                messageClient.sendMessage(driverId, title, content, type);
            }
        } else {
            pushByLevelOnly(level, title, content, type);
        }
    }

    @Override
    public Map<String, Object> getWarningStatistics(String startTime, String endTime) {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", 0);
        statistics.put("lowLevelCount", 0);
        statistics.put("mediumLevelCount", 0);
        statistics.put("highLevelCount", 0);
        statistics.put("pendingCount", 0);
        statistics.put("handledCount", 0);
        log.info("获取预警统计：startTime={}, endTime={}", startTime, endTime);
        return statistics;
    }

    @Override
    public List<Map<String, Object>> getWarningTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = days - 1; i >= 0; i--) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", now.minusDays(i).format(formatter));
            dayData.put("count", 0);
            trend.add(dayData);
        }
        
        log.info("获取预警趋势：days={}", days);
        return trend;
    }
}
