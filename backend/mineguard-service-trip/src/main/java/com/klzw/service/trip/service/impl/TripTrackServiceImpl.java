package com.klzw.service.trip.service.impl;

import com.klzw.common.core.domain.dto.TripTrackDTO;
import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.service.trip.document.TripTrackDocument;
import com.klzw.service.trip.exception.TripException;
import com.klzw.service.trip.repository.TripTrackMongoRepository;
import com.klzw.service.trip.service.TripTrackService;
import com.klzw.service.trip.service.TripValidatorService;
import com.klzw.service.trip.vo.TripTrackVO;
import com.klzw.service.trip.vo.TripVO;
import com.klzw.service.trip.constant.TripResultCode;
import com.klzw.service.trip.enums.TripStatusEnum;
import com.klzw.common.map.domain.GeoPoint;
import com.klzw.common.map.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripTrackServiceImpl implements TripTrackService {

    private final RedisCacheService redisCacheService;
    private final TripValidatorService tripValidatorService;
    private final TripTrackMongoRepository tripTrackMongoRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public void uploadTrack(TripTrackDTO dto) {
        validateTripStatus(dto.getTripId());
        
        String redisKey = "trip:track:" + dto.getTripId();
        
        Long currentCount = redisCacheService.lSize(redisKey);
        if (currentCount == null) {
            currentCount = 0L;
        }
        dto.setPointIndex(currentCount.intValue());
        
        redisCacheService.lPush(redisKey, (Object) dto);
        redisCacheService.expire(redisKey, 2, java.util.concurrent.TimeUnit.DAYS);
        
        // 同时将 vehicleId 和 tripId 的关联关系存入 Redis，用于 vehicle 模块查询
        if (dto.getVehicleId() != null) {
            String vehicleTripKey = "trip:track:vehicle:" + dto.getVehicleId();
            redisCacheService.set(vehicleTripKey, dto.getTripId(), 2, java.util.concurrent.TimeUnit.DAYS);
        }
        
        // 存储司机位置到 Redis，用于调度服务查询
        if (dto.getDriverId() != null) {
            String driverLocationKey = "driver:location:" + dto.getDriverId();
            String locationJson = String.format("{\"longitude\":%f,\"latitude\":%f,\"timestamp\":%d}", 
                    dto.getLongitude(), dto.getLatitude(), System.currentTimeMillis());
            redisCacheService.set(driverLocationKey, locationJson, 2, java.util.concurrent.TimeUnit.DAYS);
        }
        
        log.debug("轨迹点存储到 Redis 成功，行程 ID：{}，车辆 ID：{}，位置：({}, {})", 
                dto.getTripId(), dto.getVehicleId(), dto.getLongitude(), dto.getLatitude());
    }

    @Override
    public void uploadTrackBatch(List<TripTrackDTO> dtoList) {
        if (dtoList.isEmpty()) {
            return;
        }
        
        Long tripId = dtoList.get(0).getTripId();
        validateTripStatus(tripId);
        
        String redisKey = "trip:track:" + tripId;
        for (TripTrackDTO dto : dtoList) {
            redisCacheService.lPush(redisKey, (Object) dto);
        }
        redisCacheService.expire(redisKey, 2, java.util.concurrent.TimeUnit.DAYS);
        
        log.info("批量上传轨迹点到Redis成功，数量：{}", dtoList.size());
    }
    
    @Override
    public List<TripTrackDTO> getTracksFromRedis(Long tripId) {
        String redisKey = "trip:track:" + tripId;
        return redisCacheService.lRange(redisKey, 0L, -1L);
    }
    
    @Override
    public void batchSaveTracks(List<TripTrackDTO> dtoList) {
        if (dtoList.isEmpty()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < dtoList.size(); i++) {
            TripTrackDTO dto = dtoList.get(i);
            if (dto.getRecordTime() == null) {
                dto.setRecordTime(System.currentTimeMillis() - (dtoList.size() - i) * 1000L);
            }
            dto.setPointIndex(i);
        }
        
        dtoList.sort(Comparator.comparingLong(TripTrackDTO::getRecordTime));
        
        for (int i = 0; i < dtoList.size(); i++) {
            dtoList.get(i).setPointIndex(i);
        }
        
        List<TripTrackDocument> documents = dtoList.stream()
                .map(dto -> {
                    TripTrackDocument doc = new TripTrackDocument();
                    BeanUtils.copyProperties(dto, doc);
                    doc.setCreateTime(now);
                    if (dto.getRecordTime() != null) {
                        doc.setRecordTime(LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(dto.getRecordTime()),
                            java.time.ZoneId.systemDefault()
                        ));
                    }
                    return doc;
                })
                .collect(Collectors.toList());
        
        tripTrackMongoRepository.saveAll(documents);
        
        if (!dtoList.isEmpty()) {
            Long tripId = dtoList.get(0).getTripId();
            deleteTracksFromRedis(tripId);
        }
        
        log.info("批量写入轨迹点到MongoDB成功，数量：{}", documents.size());
    }
    
    @Override
    public void deleteTracksFromRedis(Long tripId) {
        String redisKey = "trip:track:" + tripId;
        redisCacheService.delete(redisKey);
        log.debug("删除Redis中的轨迹数据，行程ID：{}", tripId);
    }
    
    @Override
    public void deleteVehicleTripRelation(Long vehicleId) {
        if (vehicleId == null) {
            return;
        }
        String vehicleTripKey = "trip:track:vehicle:" + vehicleId;
        redisCacheService.delete(vehicleTripKey);
        log.debug("删除Redis中车辆与行程的关联关系，车辆ID：{}", vehicleId);
    }
    
    private void validateTripStatus(Long tripId) {
        tripValidatorService.validateTripInProgress(tripId);
    }

    @Override
    public List<TripTrackVO> getByTripId(Long tripId) {
        try {
            List<TripTrackDTO> redisTracks = getTracksFromRedis(tripId);
            if (!redisTracks.isEmpty()) {
                log.debug("从Redis获取轨迹点成功，行程ID：{}，数量：{}", tripId, redisTracks.size());
                return redisTracks.stream()
                        .sorted(Comparator.comparingLong(TripTrackDTO::getRecordTime))
                        .map(this::convertToVO)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("从Redis获取轨迹点失败，降级到MongoDB查询：行程ID={}，错误={}", tripId, e.getMessage());
            try {
                deleteTracksFromRedis(tripId);
            } catch (Exception ignored) {
            }
        }
        
        Query query = new Query(Criteria.where("tripId").is(tripId));
        List<TripTrackDocument> tracks = mongoTemplate.find(query, TripTrackDocument.class);
        
        boolean hasPointIndex = tracks.stream().anyMatch(t -> t.getPointIndex() != null);
        
        if (hasPointIndex) {
            tracks.sort(Comparator.comparing(t -> t.getPointIndex() != null ? t.getPointIndex() : Integer.MAX_VALUE));
        } else {
            tracks.sort((a, b) -> {
                LocalDateTime timeA = a.getRecordTime() != null ? a.getRecordTime() : 
                    (a.getCreateTime() != null ? a.getCreateTime() : LocalDateTime.MIN);
                LocalDateTime timeB = b.getRecordTime() != null ? b.getRecordTime() : 
                    (b.getCreateTime() != null ? b.getCreateTime() : LocalDateTime.MIN);
                return timeA.compareTo(timeB);
            });
        }
        
        return tracks.stream()
                .map(this::convertDocumentToVO)
                .collect(Collectors.toList());
    }

    @Override
    public TripTrackVO getLatestTrack(Long tripId) {
        List<TripTrackDTO> redisTracks = getTracksFromRedis(tripId);
        if (!redisTracks.isEmpty()) {
            TripTrackDTO latestTrack = redisTracks.get(0);
            log.debug("从Redis获取最新轨迹点成功，行程ID：{}", tripId);
            return convertToVO(latestTrack);
        }
        
        Query query = new Query(Criteria.where("tripId").is(tripId))
                .with(Sort.by(Sort.Direction.DESC, "recordTime"))
                .limit(1);
        
        TripTrackDocument track = mongoTemplate.findOne(query, TripTrackDocument.class);
        if (track == null) {
            return null;
        }
        return convertDocumentToVO(track);
    }
    
    private TripTrackVO convertToVO(TripTrackDTO dto) {
        TripTrackVO vo = new TripTrackVO();
        vo.setTripId(dto.getTripId().toString());
        vo.setVehicleId(dto.getVehicleId() != null ? dto.getVehicleId().toString() : null);
        vo.setLongitude(dto.getLongitude());
        vo.setLatitude(dto.getLatitude());
        vo.setSpeed(dto.getSpeed());
        vo.setDirection(dto.getDirection());
        vo.setAltitude(dto.getAltitude());
        vo.setPointIndex(dto.getPointIndex());
        if (dto.getRecordTime() != null) {
            vo.setTimestamp(LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(dto.getRecordTime()),
                java.time.ZoneId.systemDefault()
            ));
        }
        return vo;
    }
    
    private TripTrackVO convertDocumentToVO(TripTrackDocument doc) {
        TripTrackVO vo = new TripTrackVO();
        vo.setTripId(doc.getTripId().toString());
        vo.setVehicleId(doc.getVehicleId() != null ? doc.getVehicleId().toString() : null);
        vo.setLongitude(doc.getLongitude());
        vo.setLatitude(doc.getLatitude());
        vo.setSpeed(doc.getSpeed());
        vo.setDirection(doc.getDirection());
        vo.setAltitude(doc.getAltitude());
        vo.setTimestamp(doc.getRecordTime());
        vo.setPointIndex(doc.getPointIndex());
        return vo;
    }
    
    @Override
    public double calculateTotalDistance(Long tripId) {
        log.info("计算轨迹总里程：tripId={}", tripId);
        
        // 先从Redis获取轨迹点
        List<TripTrackDTO> redisTracks = getTracksFromRedis(tripId);
        
        if (!redisTracks.isEmpty()) {
            // 按时间排序（从早到晚）
            List<TripTrackDTO> sortedTracks = redisTracks.stream()
                    .sorted(Comparator.comparingLong(TripTrackDTO::getRecordTime))
                    .collect(Collectors.toList());
            
            double totalDistance = calculateDistanceFromTracks(sortedTracks);
            log.info("从Redis轨迹计算总里程：tripId={}, 轨迹点数={}, 总里程={}公里", tripId, sortedTracks.size(), totalDistance);
            return totalDistance;
        }
        
        // Redis没有数据，从MongoDB获取
        Query query = new Query(Criteria.where("tripId").is(tripId));
        List<TripTrackDocument> tracks = mongoTemplate.find(query, TripTrackDocument.class);
        
        if (tracks.isEmpty()) {
            log.warn("未找到轨迹数据：tripId={}", tripId);
            return 0.0;
        }
        
        boolean hasPointIndex = tracks.stream().anyMatch(t -> t.getPointIndex() != null);
        if (hasPointIndex) {
            tracks.sort(Comparator.comparing(t -> t.getPointIndex() != null ? t.getPointIndex() : Integer.MAX_VALUE));
        } else {
            tracks.sort((a, b) -> {
                LocalDateTime timeA = a.getRecordTime() != null ? a.getRecordTime() : 
                    (a.getCreateTime() != null ? a.getCreateTime() : LocalDateTime.MIN);
                LocalDateTime timeB = b.getRecordTime() != null ? b.getRecordTime() : 
                    (b.getCreateTime() != null ? b.getCreateTime() : LocalDateTime.MIN);
                return timeA.compareTo(timeB);
            });
        }
        
        // 转换为DTO格式并计算
        List<TripTrackDTO> trackDTOs = tracks.stream()
                .map(doc -> {
                    TripTrackDTO dto = new TripTrackDTO();
                    dto.setLongitude(doc.getLongitude());
                    dto.setLatitude(doc.getLatitude());
                    dto.setRecordTime(doc.getRecordTime() != null ? 
                            doc.getRecordTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : 0L);
                    return dto;
                })
                .collect(Collectors.toList());
        
        double totalDistance = calculateDistanceFromTracks(trackDTOs);
        log.info("从MongoDB轨迹计算总里程：tripId={}, 轨迹点数={}, 总里程={}公里", tripId, trackDTOs.size(), totalDistance);
        return totalDistance;
    }
    
    private double calculateDistanceFromTracks(List<TripTrackDTO> tracks) {
        if (tracks.size() < 2) {
            return 0.0;
        }
        
        double totalDistance = 0.0;
        for (int i = 1; i < tracks.size(); i++) {
            TripTrackDTO prev = tracks.get(i - 1);
            TripTrackDTO curr = tracks.get(i);
            
            if (prev.getLongitude() != null && prev.getLatitude() != null &&
                curr.getLongitude() != null && curr.getLatitude() != null) {
                GeoPoint prevPoint = new GeoPoint(prev.getLongitude(), prev.getLatitude());
                GeoPoint currPoint = new GeoPoint(curr.getLongitude(), curr.getLatitude());
                double distance = GeoUtils.calculateDistance(prevPoint, currPoint); // 米
                totalDistance += distance;
            }
        }
        
        return totalDistance / 1000.0; // 转换为公里
    }
}
