package com.klzw.service.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.auth.annotation.IgnoreAuth;
import com.klzw.common.core.result.Result;
import com.klzw.service.user.dto.AssignRoleDTO;
import com.klzw.service.user.dto.PasswordUpdateDTO;
import com.klzw.service.user.dto.UserRegisterDTO;
import com.klzw.service.user.dto.UserUpdateDTO;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.UserVO;
import com.klzw.common.web.resolver.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @IgnoreAuth
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody UserRegisterDTO dto) {
        UserVO userVO = userService.register(dto);
        return Result.success("注册成功", userVO);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser(@CurrentUser String userId) {
        UserVO userVO = userService.getCurrentUser(userId);
        return Result.success(userVO);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<UserVO> updateUser(@PathVariable String id, @Valid @RequestBody UserUpdateDTO dto) {
        UserVO userVO = userService.updateUserInfo(id, dto);
        return Result.success("更新成功", userVO);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@CurrentUser String userId, @Valid @RequestBody PasswordUpdateDTO dto) {
        userService.updatePassword(userId, dto);
        return Result.success("密码修改成功", null);
    }

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Result<Page<UserVO>> pageUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        Page<UserVO> page = userService.pageUsers(pageNum, pageSize, username, status);
        return Result.success(page);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable String id) {
        UserVO userVO = userService.getUserVOById(id);
        return Result.success(userVO);
    }

    @Operation(summary = "禁用用户")
    @PutMapping("/{id}/disable")
    public Result<Void> disableUser(@PathVariable String id) {
        userService.disableUser(id);
        return Result.success("禁用成功", null);
    }

    @Operation(summary = "启用用户")
    @PutMapping("/{id}/enable")
    public Result<Void> enableUser(@PathVariable String id) {
        userService.enableUser(id);
        return Result.success("启用成功", null);
    }

    @Operation(summary = "为用户分配角色")
    @PostMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable String id, @Valid @RequestBody AssignRoleDTO dto) {
        userService.assignRoles(id, dto);
        return Result.success("分配成功", null);
    }

    @Operation(summary = "获取用户角色列表")
    @GetMapping("/{id}/roles")
    public Result<List<String>> getUserRoles(@PathVariable String id) {
        List<String> roles = userService.getRoleCodesByUserId(id);
        return Result.success(roles);
    }
}
