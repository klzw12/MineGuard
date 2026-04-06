package com.klzw.service.warning.controller;

import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.common.core.result.Result;
import com.klzw.service.warning.dto.WarningHandleDTO;
import com.klzw.service.warning.dto.WarningRecordDTO;
import com.klzw.service.warning.dto.WarningRuleDTO;
import com.klzw.service.warning.dto.WarningTrackDTO;
import com.klzw.service.warning.entity.WarningRecord;
import com.klzw.service.warning.service.WarningRecordService;
import com.klzw.service.warning.service.WarningRuleService;
import com.klzw.service.warning.vo.WarningRecordVO;
import com.klzw.service.warning.vo.WarningRuleVO;
import com.klzw.service.warning.processor.WarningTriggerProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/warning")
@Tag(name = "预警管理", description = "预警管理接口")
@RequiredArgsConstructor
public class WarningController {

    private final WarningRecordService warningRecordService;
    private final WarningRuleService warningRuleService;
    private final WarningTriggerProcessor warningTriggerProcessor;

    @GetMapping("/record/page")
    @Operation(summary = "分页查询预警记录")
    public Result<PageResult<WarningRecordVO>> pageRecord(PageRequest pageRequest) {
        return Result.success(warningRecordService.page(pageRequest));
    }

    @GetMapping("/record/{id}")
    @Operation(summary = "获取预警记录详情")
    public Result<WarningRecordVO> getRecordById(@PathVariable Long id) {
        return Result.success(warningRecordService.getById(id));
    }

    @GetMapping("/record/trip")
    @Operation(summary = "根据行程 ID 查询预警记录")
    public Result<List<WarningRecordVO>> getRecordsByTripId(@RequestParam Long tripId) {
        return Result.success(warningRecordService.getByTripId(tripId));
    }

    @PostMapping("/record")
    @Operation(summary = "创建预警记录")
    public Result<Long> createWarning(@Valid @RequestBody WarningRecordDTO dto) {
        return Result.success(warningRecordService.createWarning(dto));
    }

    @PutMapping("/record/{id}/handle")
    @Operation(summary = "处理预警记录")
    public Result<Void> handleWarning(@PathVariable Long id, @RequestBody WarningHandleDTO dto) {
        warningRecordService.handleWarning(id, dto);
        return Result.success();
    }

    @PutMapping("/record/{id}/ignore")
    @Operation(summary = "忽略预警记录")
    public Result<Void> ignoreWarning(@PathVariable Long id) {
        warningRecordService.ignoreWarning(id);
        return Result.success();
    }

    @PostMapping("/process-event")
    @Operation(summary = "处理事件触发", description = "处理车辆上报的事件，生成预警记录")
    public Result<WarningRecord> processEventTrigger(@RequestBody EventTriggerRequest request) {
        try {
            log.debug("接收到事件触发请求：事件类型={}, 车辆ID={}", 
                    request.getEventType(), request.getTrack().getVehicleId());
            
            WarningRecord warningRecord = warningRecordService.processEventTrigger(request.getTrack(), request.getEventType());
            
            log.debug("事件触发处理完成：事件类型={}, 车辆ID={}", 
                    request.getEventType(), request.getTrack().getVehicleId());
            return Result.success(warningRecord);
        } catch (Exception e) {
            log.error("处理事件触发异常", e);
            return Result.error("处理事件触发失败");
        }
    }

    @PostMapping("/process-track")
    @Operation(summary = "处理轨迹点", description = "分析轨迹点数据，检测异常行为")
    public Result<WarningRecord> processTrack(@RequestBody WarningTrackDTO track) {
        try {
            log.debug("接收到轨迹点：车辆ID={}, 位置=({}, {})", 
                    track.getVehicleId(), track.getLongitude(), track.getLatitude());
            
            WarningRecord warningRecord = warningRecordService.processWarningTrack(track);
            
            if (warningRecord != null) {
                log.info("触发预警：预警编号={}, 级别={}", 
                        warningRecord.getWarningNo(), warningRecord.getWarningLevel());
            }
            
            return Result.success(warningRecord);
        } catch (Exception e) {
            log.error("处理轨迹点异常", e);
            return Result.error("处理轨迹点失败");
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取预警统计")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        return Result.success(warningRecordService.getWarningStatistics(startTime, endTime));
    }

    @GetMapping("/trend")
    @Operation(summary = "获取预警趋势")
    public Result<List<Map<String, Object>>> getTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(warningRecordService.getWarningTrend(days));
    }

    @GetMapping("/statistics/type")
    @Operation(summary = "获取预警类型统计")
    public Result<Map<String, Object>> getTypeStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.success(warningRecordService.getWarningTypeStatistics(startTime, endTime));
    }

    @GetMapping("/statistics/level")
    @Operation(summary = "获取预警级别统计")
    public Result<Map<String, Object>> getLevelStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.success(warningRecordService.getWarningLevelStatistics(startTime, endTime));
    }

    @PostMapping("/phone/unanswered")
    @Operation(summary = "标记电话未接")
    public Result<Void> markPhoneUnanswered(@RequestParam Long vehicleId) {
        warningTriggerProcessor.markPhoneUnanswered(vehicleId);
        return Result.success();
    }

    @DeleteMapping("/phone/unanswered/clear")
    @Operation(summary = "清除电话未接标记")
    public Result<Void> clearPhoneUnanswered(@RequestParam Long vehicleId) {
        warningTriggerProcessor.clearPhoneUnanswered(vehicleId);
        return Result.success();
    }

    @PostMapping("/safe-zone/mark")
    @Operation(summary = "标记车辆安全区域状态")
    public Result<Void> markVehicleInSafeZone(@RequestParam Long vehicleId, @RequestParam Boolean inSafeZone) {
        warningTriggerProcessor.markVehicleInSafeZone(vehicleId, inSafeZone);
        return Result.success();
    }

    @GetMapping("/rule/page")
    @Operation(summary = "分页查询预警规则")
    public Result<PageResult<WarningRuleVO>> pageRule(PageRequest pageRequest) {
        return Result.success(warningRuleService.page(pageRequest));
    }

    @GetMapping("/rule/{id}")
    @Operation(summary = "获取预警规则详情")
    public Result<WarningRuleVO> getRuleById(@PathVariable Long id) {
        return Result.success(warningRuleService.getById(id));
    }

    @GetMapping("/rule/code/{ruleCode}")
    @Operation(summary = "根据编码获取预警规则")
    public Result<WarningRuleVO> getRuleByCode(@PathVariable String ruleCode) {
        return Result.success(warningRuleService.getByCode(ruleCode));
    }

    @PostMapping("/rule")
    @Operation(summary = "创建预警规则")
    public Result<Long> createRule(@Valid @RequestBody WarningRuleDTO dto) {
        return Result.success(warningRuleService.create(dto));
    }

    @PutMapping("/rule/{id}")
    @Operation(summary = "更新预警规则")
    public Result<Void> updateRule(@PathVariable Long id, @Valid @RequestBody WarningRuleDTO dto) {
        warningRuleService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/rule/{id}")
    @Operation(summary = "删除预警规则")
    public Result<Void> deleteRule(@PathVariable Long id) {
        warningRuleService.delete(id);
        return Result.success();
    }

    @GetMapping("/rule/list")
    @Operation(summary = "获取所有启用的预警规则")
    public Result<List<WarningRuleVO>> listAllRules() {
        return Result.success(warningRuleService.listAll());
    }

    @PutMapping("/rule/{id}/enable")
    @Operation(summary = "启用预警规则")
    public Result<Void> enableRule(@PathVariable Long id) {
        warningRuleService.enable(id);
        return Result.success();
    }

    @PutMapping("/rule/{id}/disable")
    @Operation(summary = "禁用预警规则")
    public Result<Void> disableRule(@PathVariable Long id) {
        warningRuleService.disable(id);
        return Result.success();
    }

    public static class EventTriggerRequest {
        private WarningTrackDTO track;
        private String eventType;

        public WarningTrackDTO getTrack() {
            return track;
        }

        public void setTrack(WarningTrackDTO track) {
            this.track = track;
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }
    }
}
