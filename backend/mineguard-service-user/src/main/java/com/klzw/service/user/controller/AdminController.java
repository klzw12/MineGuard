package com.klzw.service.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.web.resolver.CurrentUser;
import com.klzw.common.core.result.Result;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.entity.RoleChangeApply;
import com.klzw.service.user.exception.UserException;
import com.klzw.service.user.constant.UserResultCode;
import com.klzw.service.user.service.RoleChangeApplyService;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.RoleChangeApplyVO;
import com.klzw.service.user.vo.UserVO;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        Page<UserVO> page = userService.pageUsers(pageNum, pageSize, username, status);
        return Result.success(page);
    }

    @Operation(summary = "变更用户角色（管理员专用）")
    @PutMapping("/user/{id}/role-change")
    public Result<Void> changeUserRole(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "新角色ID") @RequestParam Long roleId,
            @Parameter(description = "变更原因") @RequestParam(required = false) String reason) {
        
        UserVO userVO = userService.getUserVOById(id);
        if (userVO == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND);
        }
        
        Role currentRole = userService.getRoleByUserId(id);
        if (currentRole == null) {
            userService.assignRole(id, roleId);
            return Result.success("角色分配成功", null);
        }
        
        log.info("管理员变更用户角色，用户ID：{}，原角色：{}，新角色：{}，原因：{}", 
                id, currentRole.getRoleCode(), roleId, reason);
        
        userService.assignRole(id, roleId);
        
        return Result.success("角色变更成功", null);
    }

    @Operation(summary = "提交角色变更申请（用户端）")
    @PostMapping("/role-change/apply")
    public Result<String> createRoleChangeApply(
            @CurrentUser Long userId,
            @Validated @RequestBody RoleChangeApply apply) {
        apply.setUserId(userId);
        String applyId = roleChangeApplyService.createRoleChangeApply(apply);
        return Result.success("申请提交成功", applyId);
    }

    @Operation(summary = "获取我的角色变更申请（用户端）")
    @GetMapping("/role-change/apply/my")
    public Result<List<RoleChangeApplyVO>> getMyRoleChangeApplies(@CurrentUser Long userId) {
        List<RoleChangeApplyVO> applies = roleChangeApplyService.getRoleChangeAppliesByUserId(userId);
        return Result.success(applies);
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
            @RequestParam Integer status,
            @RequestParam(required = false) String adminOpinion,
            @RequestParam Long handlerId,
            @RequestParam String handlerName) {
        boolean result = roleChangeApplyService.handleRoleChangeApply(id, status, adminOpinion, handlerId, handlerName);
        if (result) {
            return Result.success("处理成功", null);
        } else {
            return Result.fail("处理失败");
        }
    }
}
