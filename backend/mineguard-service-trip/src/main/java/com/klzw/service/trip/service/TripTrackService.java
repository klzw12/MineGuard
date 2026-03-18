package com.klzw.service.trip.service;

import com.klzw.service.trip.dto.TripTrackDTO;
import com.klzw.service.trip.vo.TripTrackVO;

import java.util.List;

public interface TripTrackService {

    void uploadTrack(TripTrackDTO dto);

    void uploadTrackBatch(List<TripTrackDTO> dtoList);

    List<TripTrackVO> getByTripId(Long tripId);

    TripTrackVO getLatestTrack(Long tripId);
    
    // 从Redis获取轨迹点
    List<TripTrackDTO> getTracksFromRedis(Long tripId);
    
    // 批量写入轨迹点到MySQL
    void batchSaveTracks(List<TripTrackDTO> dtoList);
    
    // 删除Redis中的轨迹数据
    void deleteTracksFromRedis(Long tripId);
}
