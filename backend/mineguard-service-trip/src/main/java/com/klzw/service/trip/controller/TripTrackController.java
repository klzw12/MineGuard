package com.klzw.service.trip.controller;

import com.klzw.common.core.result.Result;
import com.klzw.common.core.domain.dto.TripTrackDTO;
import com.klzw.service.trip.service.TripTrackService;
import com.klzw.service.trip.vo.TripTrackVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trip/track")
@Tag(name = "轨迹管理", description = "轨迹管理接口")
@RequiredArgsConstructor
public class TripTrackController {

    private final TripTrackService tripTrackService;

    @PostMapping
    @Operation(summary = "上传轨迹点")
    public Result<Void> uploadTrack(@RequestBody TripTrackDTO dto) {
        tripTrackService.uploadTrack(dto);
        return Result.success();
    }

    @PostMapping("/batch")
    @Operation(summary = "批量上传轨迹点")
    public Result<Void> uploadTrackBatch(@RequestBody List<TripTrackDTO> dtoList) {
        tripTrackService.uploadTrackBatch(dtoList);
        return Result.success();
    }

    @GetMapping("/trip/{tripId}")
    @Operation(summary = "获取行程轨迹")
    public Result<List<TripTrackVO>> getByTripId(@PathVariable Long tripId) {
        return Result.success(tripTrackService.getByTripId(tripId));
    }

    @GetMapping("/trip/{tripId}/latest")
    @Operation(summary = "获取最新轨迹点")
    public Result<TripTrackVO> getLatestTrack(@PathVariable Long tripId) {
        return Result.success(tripTrackService.getLatestTrack(tripId));
    }
}
