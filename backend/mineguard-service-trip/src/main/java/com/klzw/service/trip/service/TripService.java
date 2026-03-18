package com.klzw.service.trip.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.common.core.domain.dto.TripResponse;
import com.klzw.service.trip.dto.TripDTO;
import com.klzw.service.trip.entity.Trip;
import com.klzw.service.trip.vo.TripStatisticsVO;
import com.klzw.service.trip.vo.TripVO;

import java.util.List;

public interface TripService extends IService<Trip> {

    PageResult<TripVO> page(PageRequest pageRequest);

    TripVO getById(Long id);

    Long create(TripDTO dto);

    void update(Long id, TripDTO dto);

    void delete(Long id);

    void startTrip(Long id);

    void endTrip(Long id, Double endLongitude, Double endLatitude);

    List<TripVO> getByVehicleId(Long vehicleId);

    List<TripVO> getByDriverId(Long driverId);

    TripVO getByTripNo(String tripNo);

    void acceptTrip(Long id);
    
    TripResponse getLatestTripByVehicleId(Long vehicleId);
    
    void pauseTrip(Long id);
    
    void resumeTrip(Long id);
    
    TripStatisticsVO getTripStatistics(Long id);
}