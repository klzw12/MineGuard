package com.klzw.service.trip.controller;

import com.klzw.service.trip.entity.Trip;
import com.klzw.service.trip.enums.TripStatusEnum;
import com.klzw.service.trip.exception.TripException;
import com.klzw.service.trip.mapper.TripMapper;
import com.klzw.service.trip.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/trip/warning")
@RequiredArgsConstructor
public class WarningCallbackController {

    private final TripService tripService;
    private final TripMapper tripMapper;

    @PostMapping("/callback")
    public void handleWarningCallback(@RequestBody WarningCallbackRequest request) {
        log.info("接收到预警回调：行程ID={}, 预警ID={}, 预警类型={}, 预警级别={}, 处理结果={}",
                request.getTripId(), request.getWarningId(), request.getWarningType(),
                request.getWarningLevel(), request.getHandleResult());

        if (request.getTripId() == null) {
            log.error("预警回调缺少行程ID，忽略处理");
            return;
        }

        Trip trip = tripMapper.selectById(request.getTripId());
        if (trip == null) {
            log.error("预警回调：行程不存在，行程ID={}", request.getTripId());
            return;
        }

        int currentStatus = trip.getStatus();
        Integer warningLevel = request.getWarningLevel() != null ? request.getWarningLevel() : 1;

        if (currentStatus != TripStatusEnum.IN_PROGRESS.getCode()
                && currentStatus != TripStatusEnum.PAUSED.getCode()) {
            log.warn("预警回调：行程不在进行中或暂停状态，当前状态={}，行程ID={}", currentStatus, request.getTripId());
            return;
        }

        if ("pause".equalsIgnoreCase(request.getHandleResult())
                || (warningLevel != null && warningLevel >= 3 && !"continue".equalsIgnoreCase(request.getHandleResult()))) {
            if (currentStatus == TripStatusEnum.IN_PROGRESS.getCode()) {
                try {
                    tripService.pauseTrip(trip.getId());
                    log.info("严重预警(级别={})，已暂停行程：行程ID={}, 预警类型={}",
                            warningLevel, trip.getId(), request.getWarningType());
                } catch (TripException e) {
                    log.error("暂停行程失败：行程ID={}, 错误={}", trip.getId(), e.getMessage());
                } catch (Exception e) {
                    log.error("暂停行程异常：行程ID={}", trip.getId(), e);
                }
            } else {
                log.info("行程已处于暂停状态，无需重复暂停：行程ID={}", trip.getId());
            }
        } else if ("continue".equalsIgnoreCase(request.getHandleResult())) {
            if (currentStatus == TripStatusEnum.PAUSED.getCode()) {
                try {
                    tripService.resumeTrip(trip.getId());
                    log.info("已恢复行程：行程ID={}, 预警类型={}", trip.getId(), request.getWarningType());
                } catch (TripException e) {
                    log.error("恢复行程失败：行程ID={}, 错误={}", trip.getId(), e.getMessage());
                } catch (Exception e) {
                    log.error("恢复行程异常：行程ID={}", trip.getId(), e);
                }
            } else {
                log.info("轻微预警，行程继续运行：行程ID={}, 预警级别={}, 预警类型={}",
                        trip.getId(), warningLevel, request.getWarningType());
            }
        } else {
            if (warningLevel >= 3) {
                log.warn("未指定处理结果的严重预警，默认暂停行程：行程ID={}, 预警级别={}",
                        trip.getId(), warningLevel);
                if (currentStatus == TripStatusEnum.IN_PROGRESS.getCode()) {
                    try {
                        tripService.pauseTrip(trip.getId());
                    } catch (Exception e) {
                        log.error("自动暂停行程异常：行程ID={}", trip.getId(), e);
                    }
                }
            } else {
                log.info("轻微预警，仅记录不干预：行程ID={}, 预警级别={}, 预警类型={}",
                        trip.getId(), warningLevel, request.getWarningType());
            }
        }

        log.info("预警回调处理完成：行程ID={}, 最终状态={}", trip.getId(),
                tripMapper.selectById(trip.getId()) != null ? tripMapper.selectById(trip.getId()).getStatus() : "未知");
    }

    private static class WarningCallbackRequest {
        private Long tripId;
        private Long warningId;
        private String warningType;
        private Integer warningLevel;
        private String handleResult;
        private String handleMessage;

        public Long getTripId() { return tripId; }
        public void setTripId(Long tripId) { this.tripId = tripId; }

        public Long getWarningId() { return warningId; }
        public void setWarningId(Long warningId) { this.warningId = warningId; }

        public String getWarningType() { return warningType; }
        public void setWarningType(String warningType) { this.warningType = warningType; }

        public Integer getWarningLevel() { return warningLevel; }
        public void setWarningLevel(Integer warningLevel) { this.warningLevel = warningLevel; }

        public String getHandleResult() { return handleResult; }
        public void setHandleResult(String handleResult) { this.handleResult = handleResult; }

        public String getHandleMessage() { return handleMessage; }
        public void setHandleMessage(String handleMessage) { this.handleMessage = handleMessage; }
    }
}
