package com.klzw.service.trip.controller;

import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.common.core.result.Result;
import com.klzw.common.core.domain.dto.TripResponse;
import com.klzw.service.trip.dto.TripDTO;
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
@RequestMapping("/api/trip")
@Tag(name = "行程管理", description = "行程管理接口")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @GetMapping("/page")
    @Operation(summary = "分页查询行程")
    public Result<PageResult<TripVO>> page(PageRequest pageRequest) {
        return Result.success(tripService.page(pageRequest));
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
            @Parameter(description = "终点经度") @RequestParam Double endLongitude,
            @Parameter(description = "终点纬度") @RequestParam Double endLatitude) {
        tripService.endTrip(id, endLongitude, endLatitude);
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

    @PostMapping("/{id}/resume")
    @Operation(summary = "恢复行程")
    public Result<Void> resumeTrip(@PathVariable Long id) {
        tripService.resumeTrip(id);
        return Result.success();
    }

    @GetMapping("/{id}/statistics")
    @Operation(summary = "获取行程统计")
    public Result<TripStatisticsVO> getTripStatistics(@PathVariable Long id) {
        return Result.success(tripService.getTripStatistics(id));
    }
}