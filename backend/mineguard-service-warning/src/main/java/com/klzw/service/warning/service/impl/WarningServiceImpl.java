package com.klzw.service.warning.service.impl;

import com.klzw.service.warning.dto.WarningTrackDTO;
import com.klzw.service.warning.entity.WarningRecord;
import com.klzw.service.warning.enums.WarningLevelEnum;
import com.klzw.service.warning.processor.EventTriggerProcessor;
import com.klzw.service.warning.processor.WarningTriggerProcessor;
import com.klzw.service.warning.service.WarningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarningServiceImpl implements WarningService {

    private final EventTriggerProcessor eventTriggerProcessor;
    private final WarningTriggerProcessor warningTriggerProcessor;

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
        Integer level = warningRecord.getWarningLevel();
        
        if (level == null) {
            return;
        }
        
        if (level.equals(WarningLevelEnum.HIGH.getCode())) {
            log.error("【高危预警】立即推送全体安全员、维修员！预警内容：{}", warningRecord.getWarningContent());
        } else if (level.equals(WarningLevelEnum.MEDIUM.getCode())) {
            log.warn("【中危预警】推送给维修员处理！预警内容：{}", warningRecord.getWarningContent());
        } else if (level.equals(WarningLevelEnum.LOW.getCode())) {
            log.info("【低危预警】推送给维修员处理！预警内容：{}", warningRecord.getWarningContent());
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
