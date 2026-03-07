package com.klzw.service.user.controller;

import com.klzw.common.auth.annotation.IgnoreAuth;
import com.klzw.common.core.result.Result;
import com.klzw.service.user.dto.RefreshTokenDTO;
import com.klzw.service.user.dto.UserLoginDTO;
import com.klzw.service.user.service.AuthService;
import com.klzw.service.user.vo.CaptchaVO;
import com.klzw.service.user.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "认证相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @IgnoreAuth
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        LoginVO loginVO = authService.login(dto);
        return Result.success("登录成功", loginVO);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        authService.logout(token);
        return Result.success("登出成功", null);
    }

    @IgnoreAuth
    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken(@RequestBody RefreshTokenDTO dto) {
        LoginVO loginVO = authService.refreshToken(dto);
        return Result.success("刷新成功", loginVO);
    }

    @IgnoreAuth
    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public Result<CaptchaVO> getCaptcha() {
        CaptchaVO captcha = authService.generateCaptcha();
        return Result.success(captcha);
    }
}
