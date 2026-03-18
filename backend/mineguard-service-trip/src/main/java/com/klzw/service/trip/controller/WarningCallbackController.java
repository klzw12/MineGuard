package com.klzw.service.trip.controller;

import com.klzw.service.trip.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/trip/warning")
@RequiredArgsConstructor
public class WarningCallbackController {

    private final TripService tripService;

    /**
     * 处理预警回调，更新行程状态
     * @param request 预警回调请求
     */
    @PostMapping("/callback")
    public void handleWarningCallback(@RequestBody WarningCallbackRequest request) {
        try {
            log.debug("接收到预警回调：行程ID={}, 预警ID={}, 处理结果={}", 
                    request.getTripId(), request.getWarningId(), request.getHandleResult());
            
            // 根据预警处理结果更新行程状态
            // 这里可以添加更复杂的逻辑，例如：
            // 1. 如果是严重预警，暂停行程
            // 2. 如果是轻微预警，继续行程
            // 3. 根据预警类型和处理结果决定行程状态
            
            // 示例：如果处理结果是"暂停行程"，则更新行程状态为暂停
            if ("pause".equals(request.getHandleResult())) {
                // 调用TripService的暂停行程方法
                tripService.pauseTrip(request.getTripId());
                log.info("暂停行程：行程ID={}", request.getTripId());
            } else if ("continue".equals(request.getHandleResult())) {
                // 调用TripService的继续行程方法
                tripService.resumeTrip(request.getTripId());
                log.info("继续行程：行程ID={}", request.getTripId());
            }
            
            log.debug("预警回调处理完成：行程ID={}", request.getTripId());
        } catch (Exception e) {
            log.error("处理预警回调异常", e);
        }
    }

    // 预警回调请求DTO
    private static class WarningCallbackRequest {
        private Long tripId;
        private Long warningId;
        private String warningType;
        private Integer warningLevel;
        private String handleResult;
        private String handleMessage;

        public Long getTripId() {
            return tripId;
        }

        public void setTripId(Long tripId) {
            this.tripId = tripId;
        }

        public Long getWarningId() {
            return warningId;
        }

        public void setWarningId(Long warningId) {
            this.warningId = warningId;
        }

        public String getWarningType() {
            return warningType;
        }

        public void setWarningType(String warningType) {
            this.warningType = warningType;
        }

        public Integer getWarningLevel() {
            return warningLevel;
        }

        public void setWarningLevel(Integer warningLevel) {
            this.warningLevel = warningLevel;
        }

        public String getHandleResult() {
            return handleResult;
        }

        public void setHandleResult(String handleResult) {
            this.handleResult = handleResult;
        }

        public String getHandleMessage() {
            return handleMessage;
        }

        public void setHandleMessage(String handleMessage) {
            this.handleMessage = handleMessage;
        }
    }
}
