package com.klzw.service.user.service;

import com.klzw.service.user.dto.RefreshTokenDTO;
import com.klzw.service.user.dto.UserLoginDTO;
import com.klzw.service.user.vo.CaptchaVO;
import com.klzw.service.user.vo.LoginVO;

public interface AuthService {

    LoginVO login(UserLoginDTO dto);

    void logout(String token);

    LoginVO refreshToken(RefreshTokenDTO dto);

    CaptchaVO generateCaptcha();
}
