package com.klzw.service.dispatch.controller;

import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.common.core.result.Result;
import com.klzw.service.dispatch.dto.RouteTemplateDTO;
import com.klzw.service.dispatch.service.RouteTemplateService;
import com.klzw.service.dispatch.vo.RouteTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dispatch/route-template")
@Tag(name = "路线模板管理", description = "路线模板管理接口")
@RequiredArgsConstructor
public class RouteTemplateController {

    private final RouteTemplateService routeTemplateService;

    @GetMapping("/page")
    @Operation(summary = "分页查询路线模板")
    public Result<PageResult<RouteTemplateVO>> page(PageRequest pageRequest) {
        return Result.success(routeTemplateService.page(pageRequest));
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有启用的路线模板")
    public Result<List<RouteTemplateVO>> listAll() {
        return Result.success(routeTemplateService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取路线模板详情")
    public Result<RouteTemplateVO> getById(@PathVariable Long id) {
        return Result.success(routeTemplateService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建路线模板")
    public Result<Long> create(@Valid @RequestBody RouteTemplateDTO dto) {
        return Result.success(routeTemplateService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新路线模板")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RouteTemplateDTO dto) {
        routeTemplateService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除路线模板")
    public Result<Void> delete(@PathVariable Long id) {
        routeTemplateService.delete(id);
        return Result.success();
    }


    @PutMapping("/{id}/enable")
    @Operation(summary = "启用路线模板")
    public Result<Void> enable(@PathVariable Long id) {
        routeTemplateService.enable(id);
        return Result.success();
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用路线模板")
    public Result<Void> disable(@PathVariable Long id) {
        routeTemplateService.disable(id);
        return Result.success();
    }
}
