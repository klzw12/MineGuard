package com.klzw.service.warning.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.warning.dto.WarningTrackDTO;
import com.klzw.service.warning.entity.WarningRecord;
import com.klzw.service.warning.service.WarningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/warning")
@RequiredArgsConstructor
public class WarningController {

    private final WarningService warningService;

    @PostMapping("/process-event")
    public Result<Void> processEventTrigger(@RequestBody EventTriggerRequest request) {
        try {
            log.debug("接收到事件触发请求：事件类型={}, 车辆ID={}", 
                    request.getEventType(), request.getTrack().getVehicleId());
            
            warningService.processEventTrigger(request.getTrack(), request.getEventType());
            
            log.debug("事件触发处理完成：事件类型={}, 车辆ID={}", 
                    request.getEventType(), request.getTrack().getVehicleId());
            return Result.success();
        } catch (Exception e) {
            log.error("处理事件触发异常", e);
            return Result.error("处理事件触发失败");
        }
    }

    @PostMapping("/process-track")
    public Result<WarningRecord> processTrack(@RequestBody WarningTrackDTO track) {
        try {
            log.debug("接收到轨迹点：车辆ID={}, 位置=({}, {})", 
                    track.getVehicleId(), track.getLongitude(), track.getLatitude());
            
            WarningRecord warningRecord = warningService.processWarningTrack(track);
            
            if (warningRecord != null) {
                log.info("触发预警：预警编号={}, 级别={}", 
                        warningRecord.getWarningNo(), warningRecord.getWarningLevel());
            }
            
            return Result.success(warningRecord);
        } catch (Exception e) {
            log.error("处理轨迹点异常", e);
            return Result.error("处理轨迹点失败");
        }
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            log.info("获取预警统计：startTime={}, endTime={}", startTime, endTime);
            Map<String, Object> statistics = warningService.getWarningStatistics(startTime, endTime);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取预警统计异常", e);
            return Result.error("获取预警统计失败");
        }
    }

    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> getTrend(@RequestParam(defaultValue = "7") int days) {
        try {
            log.info("获取预警趋势：days={}", days);
            List<Map<String, Object>> trend = warningService.getWarningTrend(days);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取预警趋势异常", e);
            return Result.error("获取预警趋势失败");
        }
    }

    private static class EventTriggerRequest {
        private WarningTrackDTO track;
        private String eventType;

        public WarningTrackDTO getTrack() {
            return track;
        }

        public void setTrack(WarningTrackDTO track) {
            this.track = track;
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }
    }
}
