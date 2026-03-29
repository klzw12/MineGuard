package com.klzw.service.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.result.Result;
import com.klzw.service.user.dto.PasswordUpdateDTO;
import com.klzw.service.user.dto.UserUpdateDTO;
import com.klzw.service.user.dto.UpdatePhoneDTO;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.IdCardVO;
import com.klzw.service.user.vo.UserVO;
import com.klzw.common.web.resolver.CurrentUser;
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
        UserVO userVO = userService.updatePhone(userId, dto.getNewPhone(), dto.getSmsCode());
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
        UserVO userVO = userService.uploadAvatar(userId, file);
        return Result.success("头像上传成功", userVO);
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
    
    @Operation(summary = "搜索联系人")
    @GetMapping("/contacts/search")
    public Result<Page<UserVO>> searchContacts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleCode,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<UserVO> page = userService.searchContacts(keyword, roleCode, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "检查用户是否存在")
    @GetMapping("/exists/{id}")
    public Result<Boolean> existsUser(@PathVariable Long id) {
        return Result.success(userService.existsUser(id));
    }
}
