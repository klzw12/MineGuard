package com.klzw.service.warning.manager;

import com.klzw.common.core.domain.dto.TripTrackDTO;
import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.service.warning.dto.WarningTrackDTO;
import com.klzw.service.warning.entity.WarningRecord;
import com.klzw.service.warning.processor.WarningTriggerProcessor;
import com.klzw.service.warning.service.WarningRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripWarningManager {

    private final RedisCacheService redisCacheService;
    private final WarningTriggerProcessor warningTriggerProcessor;
    private final WarningRecordService warningRecordService;

    private static final String TRACK_HISTORY_PREFIX = "trip:track:";
    private static final String HEARTBEAT_PREFIX = "vehicle:heartbeat:";
    private static final int HEARTBEAT_TIMEOUT_SECONDS = 60;
    private static final int CHECK_INTERVAL_SECONDS = 5;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
    private final Map<Long, ScheduledExecutorService> tripExecutors = new ConcurrentHashMap<>();
    private final Map<Long, Long> tripVehicleMap = new ConcurrentHashMap<>(); // 行程ID到车辆ID的映射

    /**
     * 启动行程预警检查
     * @param tripId 行程ID
     * @param vehicleId 车辆ID
     */
    public void startTripWarningCheck(Long tripId, Long vehicleId) {
        // 存储行程ID和车辆ID的映射
        tripVehicleMap.put(tripId, vehicleId);
        log.info("启动行程预警检查：行程ID={}, 车辆ID={}", tripId, vehicleId);

        // 创建行程专用的线程池
        ScheduledExecutorService tripExecutor = Executors.newSingleThreadScheduledExecutor();
        tripExecutors.put(tripId, tripExecutor);

        // 定时检查行程的预警
        tripExecutor.scheduleAtFixedRate(() -> {
            try {
                checkTripWarning(tripId, vehicleId);
            } catch (Exception e) {
                log.error("检查行程预警异常：行程ID={}", tripId, e);
            }
        }, 0, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 停止行程预警检查
     * @param tripId 行程ID
     */
    public void stopTripWarningCheck(Long tripId) {
        // 移除行程ID和车辆ID的映射
        tripVehicleMap.remove(tripId);
        log.info("停止行程预警检查：行程ID={}", tripId);

        // 关闭行程专用的线程池
        ScheduledExecutorService tripExecutor = tripExecutors.remove(tripId);
        if (tripExecutor != null) {
            tripExecutor.shutdown();
            try {
                if (!tripExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    tripExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                tripExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 检查行程预警
     * @param tripId 行程ID
     * @param vehicleId 车辆ID
     */
    private void checkTripWarning(Long tripId, Long vehicleId) {
        // 检查车辆心跳状态
        checkVehicleHeartbeat(vehicleId, tripId);

        // 分析车辆轨迹
        analyzeVehicleTracks(vehicleId, tripId);
    }

    /**
     * 检查车辆心跳状态
     * @param vehicleId 车辆ID
     * @param tripId 行程ID
     */
    private void checkVehicleHeartbeat(Long vehicleId, Long tripId) {
        String heartbeatKey = HEARTBEAT_PREFIX + vehicleId;
        Long lastHeartbeat = redisCacheService.get(heartbeatKey);
        if (lastHeartbeat == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if ((now - lastHeartbeat) > HEARTBEAT_TIMEOUT_SECONDS * 1000) {
            // 心跳超时，可能是通信中断，触发预警
            log.warn("车辆心跳超时：车辆ID={}, 行程ID={}", vehicleId, tripId);

            // 创建心跳超时预警
            WarningTrackDTO track = new WarningTrackDTO();
            track.setVehicleId(vehicleId);
            track.setTripId(tripId);
            
            // 处理预警
            WarningRecord warning = createHeartbeatTimeoutWarning(track);
            if (warning != null) {
                // 推送预警通知
                warningRecordService.pushWarningNotification(warning);
                log.info("心跳超时预警已处理：预警ID={}, 行程ID={}", warning.getId(), tripId);
            }
        }
    }

    /**
     * 分析车辆轨迹
     * @param vehicleId 车辆ID
     * @param tripId 行程ID
     */
    private void analyzeVehicleTracks(Long vehicleId, Long tripId) {
        String trackHistoryKey = TRACK_HISTORY_PREFIX + tripId;
        List<TripTrackDTO> history = redisCacheService.lRange(trackHistoryKey, 0L, -1L);
        if (history == null || history.isEmpty()) {
            return;
        }

        TripTrackDTO latestTripTrack = history.get(history.size() - 1);
        WarningTrackDTO latestTrack = convertToWarningTrackDTO(latestTripTrack, tripId, vehicleId);

        WarningRecord warning = warningRecordService.processWarningTrack(latestTrack);
        if (warning != null) {
            log.info("轨迹分析预警已处理：预警ID={}, 行程ID={}", warning.getId(), tripId);
        }
    }
    
    private WarningTrackDTO convertToWarningTrackDTO(TripTrackDTO tripTrack, Long tripId, Long vehicleId) {
        WarningTrackDTO dto = new WarningTrackDTO();
        dto.setTripId(tripId);
        dto.setVehicleId(vehicleId);
        dto.setDriverId(tripTrack.getDriverId());
        dto.setLongitude(tripTrack.getLongitude());
        dto.setLatitude(tripTrack.getLatitude());
        dto.setSpeed(tripTrack.getSpeed() != null ? java.math.BigDecimal.valueOf(tripTrack.getSpeed()) : null);
        dto.setDirection(tripTrack.getDirection() != null ? String.valueOf(tripTrack.getDirection()) : null);
        dto.setMileage(tripTrack.getAltitude());
        dto.setIsReported(false);
        return dto;
    }

    /**
     * 创建心跳超时预警
     * @param track 轨迹数据
     * @return 预警记录
     */
    private WarningRecord createHeartbeatTimeoutWarning(WarningTrackDTO track) {
        // 创建心跳超时预警记录
        WarningRecord warning = new WarningRecord();
        warning.setVehicleId(track.getVehicleId());
        warning.setTripId(track.getTripId());
        warning.setWarningTime(LocalDateTime.now());
        warning.setWarningType(3); // 长时间停留
        warning.setWarningLevel(2); // 中风险
        warning.setWarningContent("车辆心跳超时 - 疑似通信中断");
        warning.setStatus(0); // 待处理
        warning.setCreateTime(LocalDateTime.now());
        warning.setUpdateTime(LocalDateTime.now());
        return warning;
    }

    /**
     * 获取活跃行程列表
     * @return 活跃行程ID列表
     */
    public Set<Long> getActiveTrips() {
        return tripExecutors.keySet();
    }

    /**
     * 检查行程是否处于活跃状态
     * @param tripId 行程ID
     * @return 是否活跃
     */
    public boolean isTripActive(Long tripId) {
        return tripExecutors.containsKey(tripId);
    }

    /**
     * 获取行程对应的车辆ID
     * @param tripId 行程ID
     * @return 车辆ID
     */
    public Long getVehicleIdByTripId(Long tripId) {
        return tripVehicleMap.get(tripId);
    }
}
