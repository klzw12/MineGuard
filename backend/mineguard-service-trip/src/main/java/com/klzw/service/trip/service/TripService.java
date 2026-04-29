package com.klzw.service.trip.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.common.core.domain.dto.TripResponse;
import com.klzw.common.core.domain.dto.TripCreateRequest;
import com.klzw.service.trip.dto.TripDTO;
import com.klzw.service.trip.dto.TripEndDTO;
import com.klzw.common.core.domain.dto.TripStatisticsResponseDTO;
import com.klzw.service.trip.entity.Trip;
import com.klzw.service.trip.vo.TripStatisticsVO;
import com.klzw.service.trip.vo.TripVO;

import java.util.List;

public interface TripService extends IService<Trip> {

    PageResult<TripVO> page(PageRequest pageRequest, Integer status);

    TripVO getById(Long id);

    Long create(TripDTO dto);
    
    Long createFromDispatch(TripCreateRequest request);

    void update(Long id, TripDTO dto);

    void delete(Long id);

    void startTrip(Long id);

    void endTrip(Long id, TripEndDTO dto);

    List<TripVO> getByVehicleId(Long vehicleId);

    List<TripVO> getByDriverId(Long driverId);

    TripVO getByTripNo(String tripNo);

    void acceptTrip(Long id);
    
    TripResponse getLatestTripByVehicleId(Long vehicleId);
    
    void pauseTrip(Long id);
    
    void resumeTrip(Long id);
    
    TripStatisticsVO getTripStatistics(Long id);
    
    /**
     * 按日期范围查询行程统计（供 statistics 服务调用）
     */
    TripStatisticsResponseDTO getStatisticsByDateRange(String startDate, String endDate);
    
    /**
     * 获取行程轨迹
     */
    List<com.klzw.service.trip.vo.TripTrackVO> getTracksByTripId(Long tripId);
    
    /**
     * 获取用户的进行中行程
     */
    TripVO getActiveTrip();
    
    /**
     * 根据用户ID获取当前进行中的行程
     */
    TripVO getCurrentTripByUserId(Long userId);
    
    /**
     * 获取行程完整详情（包含AI分析、成本明细）
     */
    TripVO getTripDetail(Long id);
    
    /**
     * 获取行程成本明细列表
     */
    List<java.util.Map<String, Object>> getTripCostDetails(Long tripId);
    
    /**
     * 取消行程
     */
    void cancelTrip(Long id, String reason);
    
    /**
     * 根据调度任务ID取消行程
     */
    void cancelTripByDispatchTaskId(Long dispatchTaskId, String reason);
    
    /**
     * 更新实际货物重量（后核算）
     */
    void updateActualCargoWeight(Long id, java.math.BigDecimal actualCargoWeight);
    
    /**
     * 获取司机行程统计（供 statistics 服务调用）
     */
    java.util.Map<String, Object> getDriverStatistics(Long driverId, String startDate, String endDate);
    
    /**
     * 获取车辆行程统计（运营天数、总里程）
     */
    java.util.Map<String, Object> getVehicleTripStatistics(Long vehicleId, String startDate, String endDate);
    
    /**
     * 获取每周运营统计（每天有多少辆车有行程）
     */
    java.util.List<java.util.Map<String, Object>> getWeeklyOperationStats();
}