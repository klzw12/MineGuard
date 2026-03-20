package com.klzw.service.user.service;

import com.klzw.service.user.dto.AdminVerifyDTO;
import com.klzw.service.user.dto.RefreshTokenDTO;
import com.klzw.service.user.dto.UserLoginDTO;
import com.klzw.service.user.dto.UserRegisterDTO;
import com.klzw.service.user.dto.ResetPasswordDTO;
import com.klzw.service.user.enums.SmsScene;
import com.klzw.service.user.vo.CaptchaVO;
import com.klzw.service.user.vo.SmsCodeVO;
import com.klzw.service.user.vo.UserVO;

public interface AuthService {

    UserVO register(UserRegisterDTO dto);

    UserVO login(UserLoginDTO dto);

    void logout(String token);

    UserVO refreshToken(RefreshTokenDTO dto);

    CaptchaVO generateCaptcha();

    SmsCodeVO sendSmsCode(String phone);

    SmsCodeVO sendSmsCode(String phone, SmsScene scene);

    boolean verifySmsCode(String phone, String code);

    void resetPasswordByPhone(ResetPasswordDTO dto);

    UserVO verifyAdmin(AdminVerifyDTO dto);
}
