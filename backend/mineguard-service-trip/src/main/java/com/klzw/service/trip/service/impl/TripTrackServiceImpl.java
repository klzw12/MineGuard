package com.klzw.service.trip.service.impl;

import com.klzw.service.trip.dto.TripTrackDTO;
import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.service.trip.document.TripTrackDocument;
import com.klzw.service.trip.exception.TripException;
import com.klzw.service.trip.repository.TripTrackMongoRepository;
import com.klzw.service.trip.service.TripService;
import com.klzw.service.trip.service.TripTrackService;
import com.klzw.service.trip.vo.TripTrackVO;
import com.klzw.service.trip.vo.TripVO;
import com.klzw.service.trip.constant.TripResultCode;
import com.klzw.service.trip.enums.TripStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripTrackServiceImpl implements TripTrackService {

    private final RedisCacheService redisCacheService;
    private final TripService tripService;
    private final TripTrackMongoRepository tripTrackMongoRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public void uploadTrack(TripTrackDTO dto) {
        validateTripStatus(dto.getTripId());
        
        String redisKey = "trip:track:" + dto.getTripId();
        redisCacheService.lPush(redisKey, (Object) dto);
        
        // 同时将 vehicleId 和 tripId 的关联关系存入 Redis，用于 vehicle 模块查询
        if (dto.getVehicleId() != null) {
            String vehicleTripKey = "trip:track:vehicle:" + dto.getVehicleId();
            redisCacheService.set(vehicleTripKey, dto.getTripId(), 1, java.util.concurrent.TimeUnit.HOURS);
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
        List<TripTrackDocument> documents = dtoList.stream()
                .map(dto -> {
                    TripTrackDocument doc = new TripTrackDocument();
                    BeanUtils.copyProperties(dto, doc);
                    doc.setCreateTime(now);
                    return doc;
                })
                .collect(Collectors.toList());
        
        tripTrackMongoRepository.saveAll(documents);
        
        log.info("批量写入轨迹点到MongoDB成功，数量：{}", documents.size());
    }
    
    @Override
    public void deleteTracksFromRedis(Long tripId) {
        String redisKey = "trip:track:" + tripId;
        redisCacheService.delete(redisKey);
        log.debug("删除Redis中的轨迹数据，行程ID：{}", tripId);
    }
    
    private void validateTripStatus(Long tripId) {
        try {
            TripVO trip = tripService.getById(tripId);
            if (trip == null) {
                throw new TripException(TripResultCode.TRIP_NOT_FOUND);
            }
            if (trip.getStatus() != TripStatusEnum.IN_PROGRESS.getCode()) {
                throw new TripException(TripResultCode.TRIP_STATUS_ERROR, "只有进行中的行程可以上传轨迹");
            }
        } catch (TripException e) {
            throw e;
        } catch (Exception e) {
            throw new TripException(TripResultCode.TRIP_STATUS_ERROR, "验证行程状态失败");
        }
    }

    @Override
    public List<TripTrackVO> getByTripId(Long tripId) {
        List<TripTrackDTO> redisTracks = getTracksFromRedis(tripId);
        if (!redisTracks.isEmpty()) {
            log.debug("从Redis获取轨迹点成功，行程ID：{}，数量：{}", tripId, redisTracks.size());
            return redisTracks.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
        }
        
        Query query = new Query(Criteria.where("tripId").is(tripId))
                .with(Sort.by(Sort.Direction.ASC, "recordTime"));
        
        List<TripTrackDocument> tracks = mongoTemplate.find(query, TripTrackDocument.class);
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
        vo.setVehicleId(dto.getVehicleId().toString());
        vo.setLongitude(dto.getLongitude());
        vo.setLatitude(dto.getLatitude());
        vo.setSpeed(dto.getSpeed());
        vo.setDirection(dto.getDirection());
        vo.setAltitude(dto.getAltitude());
        vo.setTimestamp(LocalDateTime.now());
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
        return vo;
    }
}
