package com.klzw.service.trip.service.impl;

import com.klzw.service.trip.dto.TripTrackDTO;
import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.service.trip.entity.TripTrack;
import com.klzw.service.trip.exception.TripException;
import com.klzw.service.trip.mapper.TripTrackMapper;
import com.klzw.service.trip.service.TripService;
import com.klzw.service.trip.service.TripTrackService;
import com.klzw.service.trip.vo.TripTrackVO;
import com.klzw.service.trip.vo.TripVO;
import com.klzw.service.trip.constant.TripResultCode;
import com.klzw.service.trip.enums.TripStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripTrackServiceImpl implements TripTrackService {

    private final TripTrackMapper tripTrackMapper;
    private final RedisCacheService redisCacheService;
    private final TripService tripService;
    private final RestClient restClient = RestClient.create();

    private static final String WARNING_SERVICE_URL = "http://mineguard-service-warning:8080/api/warning/process-event";

    @Override
    public void uploadTrack(TripTrackDTO dto) {
        // 验证行程状态
        validateTripStatus(dto.getTripId());
        
        // 存储到Redis列表
        String redisKey = "trip:track:" + dto.getTripId();
        redisCacheService.lPush(redisKey, (Object) dto);
        log.debug("轨迹点存储到Redis成功，行程ID：{}，位置：({}, {})", 
                dto.getTripId(), dto.getLongitude(), dto.getLatitude());
        
        // 简单的预警判断逻辑
        checkForWarnings(dto);
    }

    @Override
    public void uploadTrackBatch(List<TripTrackDTO> dtoList) {
        if (dtoList.isEmpty()) {
            return;
        }
        
        // 验证行程状态
        Long tripId = dtoList.get(0).getTripId();
        validateTripStatus(tripId);
        
        // 批量存储到Redis列表
        String redisKey = "trip:track:" + tripId;
        for (TripTrackDTO dto : dtoList) {
            redisCacheService.lPush(redisKey, (Object) dto);
        }
        
        log.info("批量上传轨迹点到Redis成功，数量：{}", dtoList.size());
        
        // 批量处理预警判断
        for (TripTrackDTO dto : dtoList) {
            checkForWarnings(dto);
        }
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
        List<TripTrack> tracks = dtoList.stream()
                .map(dto -> {
                    TripTrack track = new TripTrack();
                    BeanUtils.copyProperties(dto, track);
                    track.setCreateTime(now);
                    return track;
                })
                .collect(Collectors.toList());
        
        for (TripTrack track : tracks) {
            tripTrackMapper.insert(track);
        }
        
        log.info("批量写入轨迹点到MySQL成功，数量：{}", tracks.size());
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
        // 先从Redis获取轨迹点
        List<TripTrackDTO> redisTracks = getTracksFromRedis(tripId);
        if (!redisTracks.isEmpty()) {
            log.debug("从Redis获取轨迹点成功，行程ID：{}，数量：{}", tripId, redisTracks.size());
            return redisTracks.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
        }
        
        // 从数据库获取轨迹点
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TripTrack> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(TripTrack::getTripId, tripId)
               .orderByAsc(TripTrack::getRecordTime);
        
        List<TripTrack> tracks = tripTrackMapper.selectList(wrapper);
        return tracks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public TripTrackVO getLatestTrack(Long tripId) {
        // 先从Redis获取最新轨迹点
        List<TripTrackDTO> redisTracks = getTracksFromRedis(tripId);
        if (!redisTracks.isEmpty()) {
            TripTrackDTO latestTrack = redisTracks.get(0); // Redis列表是按时间倒序存储的，第一个元素是最新的
            log.debug("从Redis获取最新轨迹点成功，行程ID：{}", tripId);
            return convertToVO(latestTrack);
        }
        
        // 从数据库获取最新轨迹点
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TripTrack> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(TripTrack::getTripId, tripId)
               .orderByDesc(TripTrack::getRecordTime)
               .last("LIMIT 1");
        
        TripTrack track = tripTrackMapper.selectOne(wrapper);
        if (track == null) {
            return null;
        }
        return convertToVO(track);
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

    /**
     * 简单的预警判断逻辑
     * @param dto 轨迹点DTO
     */
    private void checkForWarnings(TripTrackDTO dto) {
        // 这里可以添加更复杂的预警判断逻辑
        // 例如：速度异常、路线偏离、长时间停留等
        
        // 模拟预警判断
        if (dto.getSpeed() != null && dto.getSpeed() > 80) {
            // 速度异常，触发预警
            triggerWarning(dto, "speed_abnormal");
        }
        
        // 其他预警判断逻辑...
    }

    /**
     * 触发预警
     * @param dto 轨迹点DTO
     * @param eventType 事件类型
     */
    private void triggerWarning(TripTrackDTO dto, String eventType) {
        try {
            // 构建请求参数
            EventTriggerRequest request = new EventTriggerRequest();
            request.setTripTrack(dto);
            request.setEventType(eventType);
            
            // 通过HTTP调用预警服务
            restClient.post()
                    .uri(URI.create(WARNING_SERVICE_URL))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            
            log.debug("触发预警成功：事件类型={}, 行程ID={}", eventType, dto.getTripId());
        } catch (Exception e) {
            log.error("触发预警失败", e);
        }
    }

    private TripTrackVO convertToVO(TripTrack track) {
        TripTrackVO vo = new TripTrackVO();
        BeanUtils.copyProperties(track, vo);
        return vo;
    }

    // 事件触发请求DTO
    private static class EventTriggerRequest {
        private TripTrackDTO tripTrack;
        private String eventType;

        public TripTrackDTO getTripTrack() {
            return tripTrack;
        }

        public void setTripTrack(TripTrackDTO tripTrack) {
            this.tripTrack = tripTrack;
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }
    }
}
