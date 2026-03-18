package com.klzw.service.user.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.user.dto.PasswordUpdateDTO;
import com.klzw.service.user.dto.UserUpdateDTO;
import com.klzw.service.user.dto.AdminCreateUserDTO;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.UserVO;
import com.klzw.common.web.resolver.CurrentUser;
import com.klzw.common.file.impl.FileUploadServiceImpl;
import com.klzw.common.file.enums.FileBusinessTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileUploadServiceImpl fileUploadService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser(@CurrentUser Long userId) {
        UserVO userVO = userService.getCurrentUser(userId);
        return Result.success(userVO);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<UserVO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        UserVO userVO = userService.updateUserInfo(id, dto);
        return Result.success("更新成功", userVO);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@CurrentUser Long userId, @Valid @RequestBody PasswordUpdateDTO dto) {
        userService.updatePassword(userId, dto);
        return Result.success("密码修改成功", null);
    }



    @Operation(summary = "获取当前用户角色")
    @GetMapping("/role")
    public Result<Role> getCurrentUserRole(@CurrentUser Long userId) {
        Role role = userService.getRoleByUserId(userId);
        return Result.success(role);
    }

    @Operation(summary = "获取当前用户角色编码")
    @GetMapping("/role-code")
    public Result<String> getCurrentUserRoleCode(@CurrentUser Long userId) {
        String roleCode = userService.getRoleCodeByUserId(userId);
        return Result.success(roleCode);
    }

    @Operation(summary = "上传用户头像")
    @PostMapping("/avatar")
    public Result<UserVO> uploadAvatar(@CurrentUser Long userId, @RequestParam("file") MultipartFile file) {
        try {
            String avatarPath = fileUploadService.upload(file, FileBusinessTypeEnum.USER_AVATAR, String.valueOf(userId));
            UserVO userVO = userService.updateAvatar(userId, avatarPath);
            return Result.success("头像上传成功", userVO);
        } catch (Exception e) {
            return Result.fail(500, "头像上传失败：" + e.getMessage());
        }
    }

    @Operation(summary = "管理员创建用户")
    @PostMapping("/admin/create")
    public Result<String> adminCreateUser(@Valid @RequestBody AdminCreateUserDTO dto) {
        String userId = userService.adminCreateUser(dto);
        return Result.success("用户创建成功", userId);
    }
}
