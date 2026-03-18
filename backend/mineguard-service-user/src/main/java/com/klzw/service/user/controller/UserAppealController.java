package com.klzw.service.user.controller;

import com.klzw.common.web.resolver.CurrentUser;
import com.klzw.common.core.result.Result;
import com.klzw.service.user.dto.HandleAppealDTO;
import com.klzw.service.user.dto.UserAppealDTO;
import com.klzw.service.user.service.UserAppealService;
import com.klzw.service.user.vo.UserAppealVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户申诉控制器
 */
@Tag(name = "用户申诉管理", description = "用户申诉相关接口")
@RestController
@RequestMapping("/user/appeal")
@RequiredArgsConstructor
public class UserAppealController {

    private final UserAppealService userAppealService;

    @Operation(summary = "提交申诉")
    @PostMapping
    public Result<String> createAppeal(@CurrentUser Long userId, @Validated @RequestBody UserAppealDTO dto) {
        String appealId = userAppealService.createAppeal(userId, dto);
        return Result.success("申诉提交成功", appealId);
    }

    @Operation(summary = "获取当前用户的申诉列表")
    @GetMapping("/my")
    public Result<List<UserAppealVO>> getMyAppeals(@CurrentUser Long userId) {
        List<UserAppealVO> appeals = userAppealService.getAppealsByUserId(userId);
        return Result.success(appeals);
    }

    @Operation(summary = "检查是否有待处理的申诉")
    @GetMapping("/pending/check")
    public Result<Boolean> hasPendingAppeal(@CurrentUser Long userId) {
        boolean hasPending = userAppealService.hasPendingAppeal(userId);
        return Result.success(hasPending);
    }

    @Operation(summary = "获取待处理的申诉列表（管理员）")
    @GetMapping("/admin/pending")
    public Result<List<UserAppealVO>> getPendingAppeals() {
        List<UserAppealVO> appeals = userAppealService.getPendingAppeals();
        return Result.success(appeals);
    }

    @Operation(summary = "获取所有申诉列表（管理员）")
    @GetMapping("/admin/list")
    public Result<List<UserAppealVO>> getAllAppeals() {
        List<UserAppealVO> appeals = userAppealService.getAllAppeals();
        return Result.success(appeals);
    }

    @Operation(summary = "处理申诉（管理员）")
    @PutMapping("/admin/{id}/handle")
    public Result<Void> handleAppeal(@PathVariable Long id, @Validated @RequestBody HandleAppealDTO dto) {
        boolean success = userAppealService.handleAppeal(id, dto);
        if (success) {
            return Result.success("申诉处理成功", null);
        } else {
            return Result.fail(500, "申诉处理失败");
        }
    }

    @Operation(summary = "根据用户ID获取申诉列表（管理员）")
    @GetMapping("/admin/user/{userId}")
    public Result<List<UserAppealVO>> getAppealsByUserId(@PathVariable Long userId) {
        List<UserAppealVO> appeals = userAppealService.getAppealsByUserId(userId);
        return Result.success(appeals);
    }
}
