package com.klzw.service.trip.controller;

import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.common.core.result.Result;
import com.klzw.common.core.domain.dto.TripResponse;
import com.klzw.common.core.domain.dto.TripCreateRequest;
import com.klzw.service.trip.dto.TripDTO;
import com.klzw.service.trip.dto.TripEndDTO;
import com.klzw.service.trip.dto.TripStatisticsResponseDTO;
import com.klzw.service.trip.service.TripService;
import com.klzw.service.trip.vo.TripStatisticsVO;
import com.klzw.service.trip.vo.TripVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trip")
@Tag(name = "行程管理", description = "行程管理接口")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @GetMapping("/page")
    @Operation(summary = "分页查询行程")
    public Result<PageResult<TripVO>> page(
            PageRequest pageRequest,
            @Parameter(description = "行程状态") @RequestParam(required = false) Integer status) {
        return Result.success(tripService.page(pageRequest, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取行程详情")
    public Result<TripVO> getById(@PathVariable Long id) {
        return Result.success(tripService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建行程")
    public Result<Long> create(@Valid @RequestBody TripDTO dto) {
        return Result.success(tripService.create(dto));
    }
    
    @PostMapping("/dispatch/create")
    @Operation(summary = "调度任务创建行程（内部调用）")
    public Result<Long> createFromDispatch(@RequestBody TripCreateRequest request) {
        return Result.success(tripService.createFromDispatch(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新行程")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TripDTO dto) {
        tripService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除行程")
    public Result<Void> delete(@PathVariable Long id) {
        tripService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "开始行程")
    public Result<Void> startTrip(@PathVariable Long id) {
        tripService.startTrip(id);
        return Result.success();
    }

    @PostMapping("/{id}/end")
    @Operation(summary = "结束行程")
    public Result<Void> endTrip(
            @PathVariable Long id,
            @RequestBody TripEndDTO dto) {
        tripService.endTrip(id, dto);
        return Result.success();
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "根据车辆ID查询行程")
    public Result<List<TripVO>> getByVehicleId(@PathVariable Long vehicleId) {
        return Result.success(tripService.getByVehicleId(vehicleId));
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "根据司机ID查询行程")
    public Result<List<TripVO>> getByDriverId(@PathVariable Long driverId) {
        return Result.success(tripService.getByDriverId(driverId));
    }

    @GetMapping("/no/{tripNo}")
    @Operation(summary = "根据行程编号查询")
    public Result<TripVO> getByTripNo(@PathVariable String tripNo) {
        return Result.success(tripService.getByTripNo(tripNo));
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "接受行程")
    public Result<Void> acceptTrip(@PathVariable Long id) {
        tripService.acceptTrip(id);
        return Result.success();
    }
    
    @GetMapping("/latest/{vehicleId}")
    @Operation(summary = "获取车辆最近行程")
    public Result<TripResponse> getLatestTrip(@PathVariable Long vehicleId) {
        return Result.success(tripService.getLatestTripByVehicleId(vehicleId));
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "暂停行程")
    public Result<Void> pauseTrip(@PathVariable Long id) {
        tripService.pauseTrip(id);
        return Result.success();
    }
    
    @PostMapping("/{id}/end/alert")
    @Operation(summary = "结束行程（预警触发）")
    public Result<Void> endTripAlert(@PathVariable Long id) {
        TripEndDTO dto = new TripEndDTO();
        tripService.endTrip(id, dto);
        return Result.success();
    }

    @PostMapping("/{id}/resume")
    @Operation(summary = "恢复行程")
    public Result<Void> resumeTrip(@PathVariable Long id) {
        tripService.resumeTrip(id);
        return Result.success();
    }

    @GetMapping("/{id}/track")
    @Operation(summary = "获取行程轨迹")
    public Result<List<com.klzw.service.trip.vo.TripTrackVO>> getTripTrack(@PathVariable Long id) {
        return Result.success(tripService.getTracksByTripId(id));
    }

    @GetMapping("/{id}/statistics")
    @Operation(summary = "获取行程统计")
    public Result<TripStatisticsVO> getTripStatistics(@PathVariable Long id) {
        return Result.success(tripService.getTripStatistics(id));
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "按日期范围查询行程统计（供 statistics 服务调用）")
    public Result<TripStatisticsResponseDTO> getStatisticsByDateRange(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        return Result.success(tripService.getStatisticsByDateRange(startDate, endDate));
    }

    @GetMapping("/statistics/driver/{driverId}")
    @Operation(summary = "获取司机行程统计（供 statistics 服务调用）")
    public Result<java.util.Map<String, Object>> getDriverStatistics(
            @PathVariable Long driverId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        return Result.success(tripService.getDriverStatistics(driverId, startDate, endDate));
    }

    @GetMapping("/active")
    @Operation(summary = "获取用户的进行中行程")
    public Result<TripVO> getActiveTrip() {
        TripVO activeTrip = tripService.getActiveTrip();
        return Result.success(activeTrip);
    }

    @GetMapping("/{id}/detail")
    @Operation(summary = "获取行程完整详情（包含AI分析、成本明细）")
    public Result<TripVO> getTripDetail(@PathVariable Long id) {
        return Result.success(tripService.getTripDetail(id));
    }

    @GetMapping("/{id}/cost-details")
    @Operation(summary = "获取行程成本明细列表")
    public Result<List<java.util.Map<String, Object>>> getTripCostDetails(@PathVariable Long id) {
        return Result.success(tripService.getTripCostDetails(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消行程")
    public Result<Void> cancelTrip(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        tripService.cancelTrip(id, reason);
        return Result.success();
    }
    
    @PostMapping("/cancel-by-dispatch/{dispatchTaskId}")
    @Operation(summary = "根据调度任务ID取消行程（内部调用）")
    public Result<Void> cancelTripByDispatchTaskId(
            @PathVariable Long dispatchTaskId,
            @RequestParam(required = false) String reason) {
        tripService.cancelTripByDispatchTaskId(dispatchTaskId, reason);
        return Result.success();
    }

    @PutMapping("/{id}/actual-cargo-weight")
    @Operation(summary = "更新实际货物重量（后核算）")
    public Result<Void> updateActualCargoWeight(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, java.math.BigDecimal> request) {
        java.math.BigDecimal actualCargoWeight = request.get("actualCargoWeight");
        tripService.updateActualCargoWeight(id, actualCargoWeight);
        return Result.success();
    }
}