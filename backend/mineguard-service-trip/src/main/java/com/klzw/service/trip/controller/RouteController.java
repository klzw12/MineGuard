package com.klzw.service.trip.controller;

import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.common.core.result.Result;
import com.klzw.service.trip.dto.RouteDTO;
import com.klzw.service.trip.service.RouteService;
import com.klzw.service.trip.vo.RouteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trip/route")
@Tag(name = "路线管理", description = "路线管理接口")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @GetMapping("/page")
    @Operation(summary = "分页查询路线")
    public Result<PageResult<RouteVO>> page(PageRequest pageRequest) {
        return Result.success(routeService.page(pageRequest));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取路线详情")
    public Result<RouteVO> getById(@PathVariable Long id) {
        return Result.success(routeService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建路线")
    public Result<Long> create(@Valid @RequestBody RouteDTO dto) {
        return Result.success(routeService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新路线")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RouteDTO dto) {
        routeService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除路线")
    public Result<Void> delete(@PathVariable Long id) {
        routeService.delete(id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有启用的路线")
    public Result<List<RouteVO>> listAll() {
        return Result.success(routeService.listAll());
    }

    @PutMapping("/{id}/enable")
    @Operation(summary = "启用路线")
    public Result<Void> enable(@PathVariable Long id) {
        routeService.enable(id);
        return Result.success();
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用路线")
    public Result<Void> disable(@PathVariable Long id) {
        routeService.disable(id);
        return Result.success();
    }
}
