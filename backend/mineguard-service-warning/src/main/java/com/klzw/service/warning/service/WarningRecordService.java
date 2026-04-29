package com.klzw.service.warning.service;

import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.service.warning.dto.WarningHandleDTO;
import com.klzw.service.warning.dto.WarningRecordDTO;
import com.klzw.service.warning.dto.WarningTrackDTO;
import com.klzw.service.warning.entity.WarningRecord;
import com.klzw.service.warning.vo.WarningRecordVO;

import java.util.List;
import java.util.Map;

public interface WarningRecordService {

    PageResult<WarningRecordVO> page(PageRequest pageRequest);

    PageResult<WarningRecordVO> pageWithFilters(PageRequest pageRequest, Integer status, Integer warningLevel, Integer warningType, Long vehicleId, Long driverId);

    WarningRecordVO getById(Long id);
    
    List<WarningRecordVO> getByTripId(Long tripId);

    WarningRecord processEventTrigger(WarningTrackDTO tripTrack, String eventType);

    WarningRecord processWarningTrack(WarningTrackDTO track);

    Long createWarning(WarningRecordDTO dto);

    void handleWarning(Long id, WarningHandleDTO dto);

    void ignoreWarning(Long id);

    void pushWarningNotification(WarningRecord warningRecord);

    Map<String, Object> getWarningStatistics(String startTime, String endTime);

    List<Map<String, Object>> getWarningTrend(int days);

    Map<String, Object> getWarningTypeStatistics(String startTime, String endTime);

    Map<String, Object> getWarningLevelStatistics(String startTime, String endTime);

    void handleWarningsByVehicleId(Long vehicleId, Integer status, String handleResult);
}
