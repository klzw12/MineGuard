package com.klzw.service.warning.service;

import com.klzw.service.warning.dto.WarningTrackDTO;
import com.klzw.service.warning.entity.WarningRecord;

import java.util.List;
import java.util.Map;

public interface WarningService {

    WarningRecord processEventTrigger(WarningTrackDTO tripTrack, String eventType);

    WarningRecord processWarningTrack(WarningTrackDTO track);

    boolean saveWarningRecord(WarningRecord warningRecord);

    boolean updateWarningStatus(Long id, Integer status);

    void pushWarningNotification(WarningRecord warningRecord);

    Map<String, Object> getWarningStatistics(String startTime, String endTime);

    List<Map<String, Object>> getWarningTrend(int days);
}
