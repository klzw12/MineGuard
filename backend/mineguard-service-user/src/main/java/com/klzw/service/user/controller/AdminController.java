package com.klzw.service.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.result.Result;
import com.klzw.service.user.dto.AdminCreateUserDTO;
import com.klzw.service.user.dto.HandleRoleChangeApplyDTO;
import com.klzw.service.user.service.RoleChangeApplyService;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.RoleChangeApplyVO;
import com.klzw.service.user.vo.UserVO;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员管理", description = "管理员相关接口")
@RestController
@RequestMapping("/user/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserService userService;
    private final RoleChangeApplyService roleChangeApplyService;

    @Operation(summary = "分页查询用户（管理员）")
    @GetMapping("/page")
    public Result<Page<UserVO>> pageUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<UserVO> page = userService.pageUsers(pageNum, pageSize, keyword, status);
        return Result.success(page);
    }

    @Operation(summary = "管理员创建用户")
    @PostMapping("/create")
    public Result<UserVO> adminCreateUser(@Valid @RequestBody AdminCreateUserDTO dto) {
        UserVO userVO = userService.adminCreateUser(dto);
        return Result.success("用户创建成功", userVO);
    }

    @Operation(summary = "禁用用户（管理员）")
    @PutMapping("/{id}/disable")
    public Result<Void> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return Result.success("用户已禁用", null);
    }

    @Operation(summary = "启用用户（管理员）")
    @PutMapping("/{id}/enable")
    public Result<Void> enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return Result.success("用户已启用", null);
    }

    @Operation(summary = "获取待处理的角色变更申请（管理员）")
    @GetMapping("/role-change/apply/pending")
    public Result<List<RoleChangeApplyVO>> getPendingRoleChangeApplies() {
        List<RoleChangeApplyVO> applies = roleChangeApplyService.getPendingRoleChangeApplies();
        return Result.success(applies);
    }

    @Operation(summary = "获取所有角色变更申请（管理员）")
    @GetMapping("/role-change/apply/list")
    public Result<List<RoleChangeApplyVO>> getAllRoleChangeApplies() {
        List<RoleChangeApplyVO> applies = roleChangeApplyService.getAllRoleChangeApplies();
        return Result.success(applies);
    }

    @Operation(summary = "获取用户的角色变更申请历史（管理员）")
    @GetMapping("/role-change/apply/user/{userId}")
    public Result<List<RoleChangeApplyVO>> getRoleChangeAppliesByUserId(@PathVariable Long userId) {
        List<RoleChangeApplyVO> applies = roleChangeApplyService.getRoleChangeAppliesByUserId(userId);
        return Result.success(applies);
    }

    @Operation(summary = "处理角色变更申请（管理员）")
    @PutMapping("/role-change/apply/{id}/handle")
    public Result<Void> handleRoleChangeApply(
            @PathVariable Long id,
            @Validated @RequestBody HandleRoleChangeApplyDTO dto) {
        boolean result = roleChangeApplyService.handleRoleChangeApply(
            id, dto.getStatus(), dto.getAdminOpinion(), dto.getHandlerId(), dto.getHandlerName());
        if (result) {
            return Result.success("处理成功", null);
        } else {
            return Result.fail("处理失败");
        }
    }
}
