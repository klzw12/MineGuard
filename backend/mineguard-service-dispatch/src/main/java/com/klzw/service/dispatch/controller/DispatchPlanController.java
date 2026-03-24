package com.klzw.service.dispatch.controller;

import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.common.core.result.Result;
import com.klzw.service.dispatch.dto.DispatchPlanDTO;
import com.klzw.service.dispatch.service.DispatchPlanService;
import com.klzw.service.dispatch.vo.DispatchPlanVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/dispatch/plan")
@Tag(name = "调度计划管理", description = "调度计划管理接口")
@RequiredArgsConstructor
public class DispatchPlanController {

    private final DispatchPlanService dispatchPlanService;

    @GetMapping("/page")
    @Operation(summary = "分页查询调度计划")
    public Result<PageResult<DispatchPlanVO>> page(PageRequest pageRequest) {
        return Result.success(dispatchPlanService.page(pageRequest));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取调度计划详情")
    public Result<DispatchPlanVO> getById(@PathVariable Long id) {
        return Result.success(dispatchPlanService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建调度计划")
    public Result<Long> create(@Valid @RequestBody DispatchPlanDTO dto) {
        return Result.success(dispatchPlanService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新调度计划")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DispatchPlanDTO dto) {
        dispatchPlanService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除调度计划")
    public Result<Void> delete(@PathVariable Long id) {
        dispatchPlanService.delete(id);
        return Result.success();
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "根据日期查询调度计划")
    public Result<List<DispatchPlanVO>> getByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(dispatchPlanService.getByDate(date));
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "执行调度计划")
    public Result<Void> execute(@PathVariable Long id) {
        dispatchPlanService.execute(id);
        return Result.success();
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "完成调度计划")
    public Result<Void> complete(@PathVariable Long id) {
        dispatchPlanService.complete(id);
        return Result.success();
    }
}
