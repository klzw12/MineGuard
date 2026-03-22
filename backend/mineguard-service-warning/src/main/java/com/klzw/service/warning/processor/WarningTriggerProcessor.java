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
    private static final double ABNORMAL_OFFSET_THRESHOLD = 0.05;
    private static final double SAFE_ZONE_RADIUS = 0.5;
    private static final int LONG_STAY_MINUTES = 30;
    private static final int THEFT_CHECK_DURATION_MINUTES = 10;
    private static final int THEFT_MIN_TRACK_COUNT = 5;
    private static final double SPEED_LIMIT_KMH = 60.0;
    private static final double ROUTE_DEVIATION_METERS = 100.0;
    
    private static final int FATIGUE_DRIVING_THRESHOLD_MINUTES = 240;
    private static final String FATIGUE_DRIVING_KEY = "vehicle:fatigue:";
    private static final int FATIGUE_DRIVING_EXPIRE_HOURS = 8;

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
        
        WarningRecord speedWarning = checkSpeedAbnormal(track);
        if (speedWarning != null) {
            return speedWarning;
        }
        
        WarningRecord routeWarning = checkRouteDeviation(track);
        if (routeWarning != null) {
            return routeWarning;
        }
        
        WarningRecord theftWarning = checkTheftBehavior(track, history);
        if (theftWarning != null) {
            return theftWarning;
        }
        
        WarningRecord fatigueWarning = checkFatigueDriving(track);
        if (fatigueWarning != null) {
            return fatigueWarning;
        }
        
        if (isAbnormalOffset(track, history)) {
            warning = createWarningRecord(track);
            warning.setWarningType(WarningTypeEnum.DANGER_ZONE.getCode());
            warning.setWarningLevel(WarningLevelEnum.HIGH.getCode());
            warning.setWarningContent("轨迹剧烈偏移 - 疑似坠崖");
            log.error("高危预警：轨迹剧烈偏移，车辆 ID={}", track.getVehicleId());
        }
        
        return warning;
    }

    private WarningRecord checkSpeedAbnormal(WarningTrackDTO track) {
        if (track.getSpeed() == null || track.getSpeed().doubleValue() <= 0) {
            return null;
        }
        
        Double speedLimit = getSpeedLimit(track.getVehicleId());
        if (speedLimit == null) {
            speedLimit = SPEED_LIMIT_KMH;
        }
        
        double currentSpeed = track.getSpeed().doubleValue();
        if (currentSpeed > speedLimit * 1.2) {
            WarningRecord warning = createWarningRecord(track);
            warning.setWarningType(WarningTypeEnum.SPEED_ABNORMAL.getCode());
            
            if (currentSpeed > speedLimit * 1.5) {
                warning.setWarningLevel(WarningLevelEnum.HIGH.getCode());
                warning.setWarningContent(String.format("严重超速：当前速度%.1fkm/h，限速%.1fkm/h，超速%.0f%%", 
                    currentSpeed, speedLimit, (currentSpeed / speedLimit - 1) * 100));
            } else {
                warning.setWarningLevel(WarningLevelEnum.MEDIUM.getCode());
                warning.setWarningContent(String.format("超速行驶：当前速度%.1fkm/h，限速%.1fkm/h，超速%.0f%%", 
                    currentSpeed, speedLimit, (currentSpeed / speedLimit - 1) * 100));
            }
            
            log.warn("速度异常预警：车辆ID={}, 当前速度={}, 限速={}", 
                track.getVehicleId(), currentSpeed, speedLimit);
            return warning;
        }
        
        return null;
    }

    private Double getSpeedLimit(Long vehicleId) {
        String key = "vehicle:speed:limit:" + vehicleId;
        Double limit = redisCacheService.get(key);
        return limit;
    }

    public void setSpeedLimit(Long vehicleId, Double speedLimit) {
        String key = "vehicle:speed:limit:" + vehicleId;
        redisCacheService.set(key, speedLimit, 24, TimeUnit.HOURS);
        log.info("设置车辆限速：车辆ID={}, 限速={}km/h", vehicleId, speedLimit);
    }

    private WarningRecord checkRouteDeviation(WarningTrackDTO track) {
        String routeKey = "vehicle:route:planned:" + track.getVehicleId();
        List<GeoPoint> plannedRoute = redisCacheService.get(routeKey);
        
        if (plannedRoute == null || plannedRoute.isEmpty()) {
            return null;
        }
        
        GeoPoint currentPoint = new GeoPoint(track.getLongitude(), track.getLatitude());
        
        double minDistance = Double.MAX_VALUE;
        for (GeoPoint routePoint : plannedRoute) {
            double distance = GeoUtils.calculateDistance(currentPoint, routePoint);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        
        if (minDistance > ROUTE_DEVIATION_METERS) {
            WarningRecord warning = createWarningRecord(track);
            warning.setWarningType(WarningTypeEnum.ROUTE_DEVIATION.getCode());
            
            if (minDistance > ROUTE_DEVIATION_METERS * 3) {
                warning.setWarningLevel(WarningLevelEnum.HIGH.getCode());
                warning.setWarningContent(String.format("严重偏离路线：偏离%.0f米", minDistance));
            } else {
                warning.setWarningLevel(WarningLevelEnum.MEDIUM.getCode());
                warning.setWarningContent(String.format("偏离路线：偏离%.0f米", minDistance));
            }
            
            log.warn("路线偏离预警：车辆ID={}, 偏离距离={}米", track.getVehicleId(), minDistance);
            return warning;
        }
        
        return null;
    }

    public void setPlannedRoute(Long vehicleId, List<GeoPoint> route) {
        String key = "vehicle:route:planned:" + vehicleId;
        redisCacheService.set(key, route, 8, TimeUnit.HOURS);
        log.info("设置规划路线：车辆ID={}, 路线点数={}", vehicleId, route.size());
    }

    private WarningRecord checkTheftBehavior(WarningTrackDTO track, List<WarningTrackDTO> history) {
        if (history.size() < THEFT_MIN_TRACK_COUNT) {
            return null;
        }
        
        boolean isInSafeZone = isInSafeZone(track);
        if (!isInSafeZone) {
            return null;
        }
        
        boolean isLongStay = checkLongStay(track, history);
        if (!isLongStay) {
            return null;
        }
        
        String checkKey = "vehicle:theft:check:" + track.getVehicleId();
        Boolean theftCheckStarted = redisCacheService.get(checkKey);
        
        if (theftCheckStarted == null) {
            startTheftCheck(track.getVehicleId());
            return null;
        }
        
        boolean isWsTimeout = isWsResponseTimeout(track.getVehicleId());
        boolean isPhoneUnanswered = checkPhoneUnanswered(track.getVehicleId());
        
        if (isWsTimeout && isPhoneUnanswered) {
            WarningRecord warning = createWarningRecord(track);
            warning.setWarningType(WarningTypeEnum.ABNORMAL_BEHAVIOR.getCode());
            warning.setWarningLevel(WarningLevelEnum.HIGH.getCode());
            warning.setWarningContent("疑似盗卸行为 - 长时间停留、WS未响应、电话未接");
            log.error("高危预警：疑似盗卸行为，车辆ID={}, 司机ID={}", 
                track.getVehicleId(), track.getDriverId());
            
            clearTheftCheck(track.getVehicleId());
            return warning;
        }
        
        return null;
    }

    private boolean checkLongStay(WarningTrackDTO current, List<WarningTrackDTO> history) {
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

    private void startTheftCheck(Long vehicleId) {
        String checkKey = "vehicle:theft:check:" + vehicleId;
        redisCacheService.set(checkKey, true, THEFT_CHECK_DURATION_MINUTES, TimeUnit.MINUTES);
        log.info("启动盗卸检测流程：车辆ID={}", vehicleId);
    }

    private void clearTheftCheck(Long vehicleId) {
        String checkKey = "vehicle:theft:check:" + vehicleId;
        redisCacheService.delete(checkKey);
        log.info("清除盗卸检测流程：车辆ID={}", vehicleId);
    }

    private boolean isWsResponseTimeout(Long vehicleId) {
        String key = "vehicle:ws:response:" + vehicleId;
        Boolean responded = redisCacheService.get(key);
        return responded == null || !responded;
    }

    public void markWsResponded(Long vehicleId) {
        String key = "vehicle:ws:response:" + vehicleId;
        redisCacheService.set(key, true, THEFT_CHECK_DURATION_MINUTES, TimeUnit.MINUTES);
        log.info("标记WS已响应：车辆ID={}", vehicleId);
    }

    private boolean isInSafeZone(WarningTrackDTO track) {
        String key = "vehicle:safe:zone:" + track.getVehicleId();
        Boolean inSafeZone = redisCacheService.get(key);
        return inSafeZone != null && inSafeZone;
    }

    private boolean checkPhoneUnanswered(Long vehicleId) {
        String key = "vehicle:phone:unanswered:" + vehicleId;
        Boolean unanswered = redisCacheService.get(key);
        return unanswered != null && unanswered;
    }

    public void markPhoneUnanswered(Long vehicleId) {
        String key = "vehicle:phone:unanswered:" + vehicleId;
        redisCacheService.set(key, true, 30, TimeUnit.MINUTES);
        log.info("标记电话未接：车辆ID={}", vehicleId);
    }

    public void clearPhoneUnanswered(Long vehicleId) {
        String key = "vehicle:phone:unanswered:" + vehicleId;
        redisCacheService.delete(key);
        log.info("清除电话未接标记：车辆ID={}", vehicleId);
    }

    public void markVehicleInSafeZone(Long vehicleId, boolean inSafeZone) {
        String key = "vehicle:safe:zone:" + vehicleId;
        redisCacheService.set(key, inSafeZone, 24, TimeUnit.HOURS);
        log.info("标记车辆安全区域状态：车辆ID={}, 在安全区域={}", vehicleId, inSafeZone);
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
    
    private WarningRecord checkFatigueDriving(WarningTrackDTO track) {
        if (track.getSpeed() == null || track.getSpeed().doubleValue() <= 0) {
            clearFatigueDriving(track.getVehicleId());
            return null;
        }
        
        Long continuousDriveMinutes = getContinuousDrivingMinutes(track.getVehicleId());
        
        if (continuousDriveMinutes >= FATIGUE_DRIVING_THRESHOLD_MINUTES) {
            WarningRecord warning = createWarningRecord(track);
            warning.setWarningType(WarningTypeEnum.FATIGUE_DRIVING.getCode());
            warning.setWarningLevel(WarningLevelEnum.MEDIUM.getCode());
            warning.setWarningContent(String.format("疲劳驾驶预警：连续驾驶%d分钟，建议休息", continuousDriveMinutes));
            log.warn("疲劳驾驶预警：车辆 ID={}, 司机 ID={}, 连续驾驶时长={}分钟", 
                track.getVehicleId(), track.getDriverId(), continuousDriveMinutes);
            
            setFatigueDrivingFlag(track.getVehicleId());
            
            return warning;
        }
        
        updateDrivingTime(track.getVehicleId());
        
        return null;
    }
    
    private Long getContinuousDrivingMinutes(Long vehicleId) {
        String key = FATIGUE_DRIVING_KEY + vehicleId;
        Long startTime = redisCacheService.get(key);
        
        if (startTime == null) {
            return 0L;
        }
        
        long now = System.currentTimeMillis();
        long duration = (now - startTime) / 1000 / 60;
        
        return duration;
    }
    
    private void updateDrivingTime(Long vehicleId) {
        String key = FATIGUE_DRIVING_KEY + vehicleId;
        Long startTime = redisCacheService.get(key);
        
        if (startTime == null) {
            redisCacheService.set(key, System.currentTimeMillis(), FATIGUE_DRIVING_EXPIRE_HOURS, TimeUnit.HOURS);
            log.info("开始记录驾驶时间：车辆 ID={}", vehicleId);
        } else {
            long now = System.currentTimeMillis();
            long duration = (now - startTime) / 1000 / 60;
            
            if (duration >= FATIGUE_DRIVING_THRESHOLD_MINUTES) {
                log.warn("疲劳驾驶持续中：车辆 ID={}, 已驾驶{}分钟", vehicleId, duration);
            }
        }
    }
    
    private void setFatigueDrivingFlag(Long vehicleId) {
        String flagKey = FATIGUE_DRIVING_KEY + "flag:" + vehicleId;
        redisCacheService.set(flagKey, true, FATIGUE_DRIVING_EXPIRE_HOURS, TimeUnit.HOURS);
    }
    
    private boolean hasFatigueDrivingFlag(Long vehicleId) {
        String flagKey = FATIGUE_DRIVING_KEY + "flag:" + vehicleId;
        Boolean hasFlag = redisCacheService.get(flagKey);
        return hasFlag != null && hasFlag;
    }
    
    private void clearFatigueDriving(Long vehicleId) {
        String key = FATIGUE_DRIVING_KEY + vehicleId;
        String flagKey = FATIGUE_DRIVING_KEY + "flag:" + vehicleId;
        redisCacheService.delete(key);
        redisCacheService.delete(flagKey);
        log.info("清除疲劳驾驶记录：车辆 ID={}", vehicleId);
    }
    
    public boolean canStartDriving(Long vehicleId) {
        String flagKey = FATIGUE_DRIVING_KEY + "flag:" + vehicleId;
        Boolean hasFlag = redisCacheService.get(flagKey);
        return hasFlag == null || !hasFlag;
    }
    
    public void clearFatigueFlag(Long vehicleId) {
        String flagKey = FATIGUE_DRIVING_KEY + "flag:" + vehicleId;
        redisCacheService.delete(flagKey);
        log.info("清除疲劳驾驶标志，允许再次启动：车辆 ID={}", vehicleId);
    }
}
