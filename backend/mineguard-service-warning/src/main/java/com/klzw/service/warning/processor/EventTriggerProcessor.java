package com.klzw.service.warning.processor;

import com.klzw.service.warning.dto.WarningTrackDTO;
import com.klzw.service.warning.entity.WarningRecord;
import com.klzw.service.warning.enums.WarningLevelEnum;
import com.klzw.service.warning.enums.WarningRecordStatusEnum;
import com.klzw.service.warning.enums.WarningTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Component
public class EventTriggerProcessor {

    private final Random random = new Random();

    /**
     * 处理事件触发，生成预警记录
     * @param tripTrack 轨迹点
     * @param eventType 事件类型
     * @return 预警记录
     */
    public WarningRecord processEventTrigger(WarningTrackDTO tripTrack, String eventType) {
        WarningRecord warningRecord = new WarningRecord();
        warningRecord.setWarningNo(generateWarningNo());
        warningRecord.setVehicleId(tripTrack.getVehicleId());
        warningRecord.setDriverId(tripTrack.getDriverId());
        warningRecord.setTripId(tripTrack.getTripId());
        warningRecord.setLongitude(tripTrack.getLongitude());
        warningRecord.setLatitude(tripTrack.getLatitude());
        warningRecord.setSpeed(tripTrack.getSpeed());
        warningRecord.setWarningTime(LocalDateTime.now());
        warningRecord.setStatus(WarningRecordStatusEnum.PENDING.getCode()); // 未处理
        warningRecord.setCreateTime(LocalDateTime.now());
        warningRecord.setUpdateTime(LocalDateTime.now());

        // 根据事件类型设置预警类型和级别
        switch (eventType) {
            case "vehicle_failure":
                // 车辆故障，低风险
                warningRecord.setWarningType(WarningTypeEnum.VEHICLE_FAULT.getCode()); // 车辆故障
                warningRecord.setWarningLevel(WarningLevelEnum.LOW.getCode()); // 低风险
                warningRecord.setWarningContent("车辆故障预警");
                break;
            case "theft":
                // 盗泻行为，中风险
                warningRecord.setWarningType(WarningTypeEnum.ABNORMAL_BEHAVIOR.getCode()); // 异常行为
                warningRecord.setWarningLevel(WarningLevelEnum.MEDIUM.getCode()); // 中风险
                warningRecord.setWarningContent("疑似盗泻行为预警");
                break;
            case "danger":
                // 危险地带，高风险
                warningRecord.setWarningType(WarningTypeEnum.DANGER_ZONE.getCode()); // 危险区域
                warningRecord.setWarningLevel(WarningLevelEnum.HIGH.getCode()); // 高风险
                warningRecord.setWarningContent("危险地带预警");
                break;
            default:
                log.warn("未知事件类型: {}", eventType);
                warningRecord.setWarningType(WarningTypeEnum.ABNORMAL_BEHAVIOR.getCode()); // 异常行为
                warningRecord.setWarningLevel(WarningLevelEnum.LOW.getCode()); // 低风险
                warningRecord.setWarningContent("未知事件预警");
        }

        log.debug("事件触发预警：事件类型={}, 预警级别={}, 位置=({}, {})", 
                eventType, warningRecord.getWarningLevel(), 
                warningRecord.getLongitude(), warningRecord.getLatitude());

        return warningRecord;
    }

    /**
     * 生成预警编号
     * @return 预警编号
     */
    private String generateWarningNo() {
        return "WARN" + System.currentTimeMillis() + random.nextInt(1000);
    }
}