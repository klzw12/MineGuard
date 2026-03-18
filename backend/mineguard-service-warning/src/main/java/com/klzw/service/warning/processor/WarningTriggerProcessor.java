package com.klzw.service.warning.processor;

import com.klzw.common.map.domain.GeoPoint;
import com.klzw.common.map.util.GeoUtils;
import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.service.warning.dto.WarningTrackDTO;
import com.klzw.service.warning.entity.WarningRecord;
import com.klzw.service.warning.enums.WarningLevelEnum;
import com.klzw.service.warning.enums.WarningRecordStatusEnum;
import com.klzw.service.warning.enums.WarningTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarningTriggerProcessor {

    private final RedisCacheService redisCacheService;
    private final Random random = new Random();

    private static final String TRACK_HISTORY_PREFIX = "vehicle:track:history:";
    private static final int TRACK_HISTORY_SIZE = 10;
    private static final int HEARTBEAT_TIMEOUT_SECONDS = 60;
    private static final double DANGER_ZONE_THRESHOLD = 0.1;
    private static final double ABNORMAL_OFFSET_THRESHOLD = 0.05;
    private static final double SAFE_ZONE_RADIUS = 0.5;

    public WarningRecord processTrack(WarningTrackDTO track) {
        List<WarningTrackDTO> history = getTrackHistory(track.getVehicleId());
        
        WarningRecord warning = null;
        
        if (track.getIsReported()) {
            warning = handleReportedEvent(track);
        } else {
            warning = analyzeTrackAnomaly(track, history);
        }
        
        saveTrackToHistory(track);
        
        return warning;
    }

    private WarningRecord handleReportedEvent(WarningTrackDTO track) {
        WarningRecord warning = createWarningRecord(track);
        
        if ("LOW_FUEL".equals(track.getSpecialStatus()) || "BROKEN_DOWN".equals(track.getSpecialStatus())) {
            warning.setWarningType(WarningTypeEnum.VEHICLE_FAULT.getCode());
            warning.setWarningLevel(WarningLevelEnum.LOW.getCode());
            warning.setWarningContent("车辆故障 - " + track.getSpecialStatus());
            log.info("低危预警：车辆故障上报，车辆ID={}", track.getVehicleId());
        } else if ("ACCIDENT".equals(track.getSpecialStatus()) || "EMERGENCY".equals(track.getSpecialStatus())) {
            warning.setWarningType(WarningTypeEnum.DANGER_ZONE.getCode());
            warning.setWarningLevel(WarningLevelEnum.HIGH.getCode());
            warning.setWarningContent("紧急情况 - " + track.getSpecialStatus());
            log.warn("高危预警：紧急情况上报，车辆ID={}", track.getVehicleId());
        }
        
        return warning;
    }

    private WarningRecord analyzeTrackAnomaly(WarningTrackDTO track, List<WarningTrackDTO> history) {
        WarningRecord warning = null;
        
        if (isHeartbeatTimeout(track.getVehicleId())) {
            warning = createWarningRecord(track);
            warning.setWarningType(WarningTypeEnum.ABNORMAL_BEHAVIOR.getCode());
            warning.setWarningLevel(WarningLevelEnum.MEDIUM.getCode());
            warning.setWarningContent("通信超时 - 疑似盗泻");
            log.warn("中危预警：通信超时，车辆ID={}", track.getVehicleId());
        } else if (isAbnormalOffset(track, history)) {
            warning = createWarningRecord(track);
            warning.setWarningType(WarningTypeEnum.DANGER_ZONE.getCode());
            warning.setWarningLevel(WarningLevelEnum.HIGH.getCode());
            warning.setWarningContent("轨迹剧烈偏移 - 疑似坠崖");
            log.error("高危预警：轨迹剧烈偏移，车辆ID={}", track.getVehicleId());
        } else if (isStayingInSafeZone(track, history)) {
            warning = createWarningRecord(track);
            warning.setWarningType(WarningTypeEnum.ABNORMAL_BEHAVIOR.getCode());
            warning.setWarningLevel(WarningLevelEnum.MEDIUM.getCode());
            warning.setWarningContent("安全区域停留超时");
            log.warn("中危预警：安全区域停留超时，车辆ID={}", track.getVehicleId());
        }
        
        return warning;
    }

    private boolean isHeartbeatTimeout(Long vehicleId) {
        String key = "vehicle:heartbeat:" + vehicleId;
        Long lastHeartbeat = redisCacheService.get(key);
        if (lastHeartbeat == null) {
            return true;
        }
        long now = System.currentTimeMillis();
        return (now - lastHeartbeat) > HEARTBEAT_TIMEOUT_SECONDS * 1000;
    }

    private boolean isAbnormalOffset(WarningTrackDTO current, List<WarningTrackDTO> history) {
        if (history.size() < 3) {
            return false;
        }
        
        WarningTrackDTO last1 = history.get(history.size() - 1);
        WarningTrackDTO last2 = history.get(history.size() - 2);
        WarningTrackDTO last3 = history.get(history.size() - 3);
        
        GeoPoint currentPoint = new GeoPoint(current.getLongitude(), current.getLatitude());
        GeoPoint last1Point = new GeoPoint(last1.getLongitude(), last1.getLatitude());
        GeoPoint last2Point = new GeoPoint(last2.getLongitude(), last2.getLatitude());
        GeoPoint last3Point = new GeoPoint(last3.getLongitude(), last3.getLatitude());
        
        double distance1 = GeoUtils.calculateDistance(currentPoint, last1Point);
        double distance2 = GeoUtils.calculateDistance(last1Point, last2Point);
        double distance3 = GeoUtils.calculateDistance(last2Point, last3Point);
        
        double avgDistance = (distance2 + distance3) / 2;
        return distance1 > avgDistance * 5 || distance1 > ABNORMAL_OFFSET_THRESHOLD * 1000;
    }

    private boolean isStayingInSafeZone(WarningTrackDTO current, List<WarningTrackDTO> history) {
        if (history.size() < 5) {
            return false;
        }
        
        GeoPoint currentPoint = new GeoPoint(current.getLongitude(), current.getLatitude());
        
        for (WarningTrackDTO hist : history) {
            GeoPoint histPoint = new GeoPoint(hist.getLongitude(), hist.getLatitude());
            double distance = GeoUtils.calculateDistance(currentPoint, histPoint);
            if (distance > SAFE_ZONE_RADIUS * 1000) {
                return false;
            }
        }
        
        return true;
    }

    private WarningRecord createWarningRecord(WarningTrackDTO track) {
        WarningRecord warning = new WarningRecord();
        warning.setWarningNo(generateWarningNo());
        warning.setVehicleId(track.getVehicleId());
        warning.setDriverId(track.getDriverId());
        warning.setTripId(track.getTripId());
        warning.setLongitude(track.getLongitude());
        warning.setLatitude(track.getLatitude());
        warning.setSpeed(track.getSpeed());
        warning.setWarningTime(LocalDateTime.now());
        warning.setStatus(WarningRecordStatusEnum.PENDING.getCode());
        warning.setCreateTime(LocalDateTime.now());
        warning.setUpdateTime(LocalDateTime.now());
        return warning;
    }

    private String generateWarningNo() {
        return "WARN" + System.currentTimeMillis() + random.nextInt(1000);
    }

    private List<WarningTrackDTO> getTrackHistory(Long vehicleId) {
        String key = TRACK_HISTORY_PREFIX + vehicleId;
        List<WarningTrackDTO> history = redisCacheService.get(key);
        return history != null ? history : new ArrayList<>();
    }

    private void saveTrackToHistory(WarningTrackDTO track) {
        String key = TRACK_HISTORY_PREFIX + track.getVehicleId();
        List<WarningTrackDTO> history = getTrackHistory(track.getVehicleId());
        
        history.add(track);
        if (history.size() > TRACK_HISTORY_SIZE) {
            history.remove(0);
        }
        
        redisCacheService.set(key, history, 1, TimeUnit.HOURS);
        
        String heartbeatKey = "vehicle:heartbeat:" + track.getVehicleId();
        redisCacheService.set(heartbeatKey, System.currentTimeMillis(), HEARTBEAT_TIMEOUT_SECONDS + 10, TimeUnit.SECONDS);
    }
}
