package com.klzw.service.trip.service;

import com.klzw.service.trip.exception.TripException;
import com.klzw.service.trip.constant.TripResultCode;
import com.klzw.service.trip.enums.TripStatusEnum;
import com.klzw.service.trip.mapper.TripMapper;
import com.klzw.service.trip.vo.TripVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripValidatorService {

    private final TripMapper tripMapper;

    public void validateTripStatus(Long tripId, int requiredStatus) {
        try {
            var trip = tripMapper.selectById(tripId);
            if (trip == null) {
                throw new TripException(TripResultCode.TRIP_NOT_FOUND);
            }
            if (trip.getStatus() != requiredStatus) {
                throw new TripException(TripResultCode.TRIP_STATUS_ERROR, "行程状态不符合要求");
            }
        } catch (TripException e) {
            throw e;
        } catch (Exception e) {
            throw new TripException(TripResultCode.TRIP_STATUS_ERROR, "验证行程状态失败");
        }
    }

    public void validateTripInProgress(Long tripId) {
        validateTripStatus(tripId, TripStatusEnum.IN_PROGRESS.getCode());
    }

    public boolean existsById(Long tripId) {
        return tripMapper.selectById(tripId) != null;
    }
}