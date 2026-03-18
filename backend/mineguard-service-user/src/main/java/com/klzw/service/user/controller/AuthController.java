package com.klzw.service.user.controller;

import com.klzw.common.auth.annotation.IgnoreAuth;
import com.klzw.common.core.result.Result;
import com.klzw.service.user.dto.AdminVerifyDTO;
import com.klzw.service.user.dto.RefreshTokenDTO;
import com.klzw.service.user.dto.SendSmsCodeDTO;
import com.klzw.service.user.dto.UserLoginDTO;
import com.klzw.service.user.dto.UserRegisterDTO;
import com.klzw.service.user.dto.ResetPasswordDTO;
import com.klzw.service.user.dto.VerifySmsCodeDTO;
import com.klzw.service.user.service.AuthService;
import com.klzw.service.user.vo.SmsCodeVO;
import com.klzw.service.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @IgnoreAuth
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<UserVO> register(@Validated @RequestBody UserRegisterDTO dto) {
        UserVO userVO = authService.register(dto);
        return Result.success("注册成功", userVO);
    }

    @IgnoreAuth
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<UserVO> login(@Validated @RequestBody UserLoginDTO dto) {
        UserVO userVO = authService.login(dto);
        return Result.success("登录成功", userVO);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        authService.logout(token);
        return Result.success("登出成功", null);
    }

    @IgnoreAuth
    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<UserVO> refreshToken(@Validated @RequestBody RefreshTokenDTO dto) {
        UserVO userVO = authService.refreshToken(dto);
        return Result.success("Token刷新成功", userVO);
    }

    @IgnoreAuth
    @Operation(summary = "发送短信验证码")
    @PostMapping("/sms/send")
    public Result<SmsCodeVO> sendSmsCode(@Validated @RequestBody SendSmsCodeDTO dto) {
        SmsCodeVO smsCodeVO = authService.sendSmsCode(dto.getPhone());
        return Result.success("短信验证码发送成功", smsCodeVO);
    }

    @IgnoreAuth
    @Operation(summary = "验证短信验证码")
    @PostMapping("/sms/verify")
    public Result<Boolean> verifySmsCode(@Validated @RequestBody VerifySmsCodeDTO dto) {
        boolean result = authService.verifySmsCode(dto.getPhone(), dto.getCode());
        return Result.success("验证成功", result);
    }

    @IgnoreAuth
    @Operation(summary = "通过手机号重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPasswordByPhone(@Validated @RequestBody ResetPasswordDTO dto) {
        authService.resetPasswordByPhone(dto);
        return Result.success("密码重置成功", null);
    }

    @Operation(summary = "管理员认证")
    @PostMapping("/admin/verify")
    public Result<UserVO> verifyAdmin(@Validated @RequestBody AdminVerifyDTO dto) {
        UserVO userVO = authService.verifyAdmin(dto);
        return Result.success("管理员认证成功", userVO);
    }

}
