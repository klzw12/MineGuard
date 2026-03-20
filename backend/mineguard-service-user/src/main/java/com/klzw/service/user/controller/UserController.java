package com.klzw.service.user.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.user.dto.PasswordUpdateDTO;
import com.klzw.service.user.dto.UserUpdateDTO;
import com.klzw.service.user.dto.AdminCreateUserDTO;
import com.klzw.service.user.dto.UpdatePhoneDTO;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.service.sms.SmsService;
import com.klzw.service.user.vo.IdCardVO;
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
    private final SmsService smsService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser(@CurrentUser Long userId) {
        UserVO userVO = userService.getCurrentUser(userId);
        return Result.success(userVO);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        UserVO userVO = userService.getUserVOById(id);
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

    @Operation(summary = "更新手机号")
    @PutMapping("/phone")
    public Result<UserVO> updatePhone(@CurrentUser Long userId, @Valid @RequestBody UpdatePhoneDTO dto) {
        boolean smsVerified = smsService.verifySmsCode(dto.getNewPhone(), dto.getSmsCode());
        if (!smsVerified) {
            return Result.fail(400, "短信验证码错误或已过期");
        }
        UserVO userVO = userService.updatePhone(userId, dto.getNewPhone());
        return Result.success("手机号更新成功", userVO);
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
    
    @Operation(summary = "获取头像签名URL")
    @GetMapping("/avatar/signed-url")
    public Result<String> getAvatarSignedUrl(@CurrentUser Long userId) {
        String signedUrl = userService.getAvatarSignedUrl(userId);
        return Result.success(signedUrl);
    }
    
    @Operation(summary = "获取身份证图片签名URL")
    @GetMapping("/id-card/signed-urls")
    public Result<IdCardVO> getIdCardSignedUrls(@CurrentUser Long userId) {
        IdCardVO idCardVO = userService.getIdCardSignedUrls(userId);
        return Result.success(idCardVO);
    }

    @Operation(summary = "根据角色获取用户列表")
    @GetMapping("/list")
    public Result<java.util.List<UserVO>> getUsersByRole(@RequestParam(required = false) String roleCode) {
        java.util.List<UserVO> users = userService.getUsersByRoleCode(roleCode);
        return Result.success(users);
    }

    @Operation(summary = "管理员创建用户")
    @PostMapping("/admin/create")
    public Result<String> adminCreateUser(@Valid @RequestBody AdminCreateUserDTO dto) {
        String userId = userService.adminCreateUser(dto);
        return Result.success("用户创建成功", userId);
    }

    @Operation(summary = "分配角色（管理员）")
    @PutMapping("/{id}/role")
    public Result<Void> assignRole(@PathVariable Long id, @RequestParam Long roleId) {
        userService.assignRole(id, roleId);
        return Result.success("角色分配成功", null);
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
}
