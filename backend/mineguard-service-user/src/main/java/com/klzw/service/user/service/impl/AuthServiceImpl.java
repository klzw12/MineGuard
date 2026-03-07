package com.klzw.service.user.service.impl;

import com.klzw.common.auth.constant.AuthResultCode;
import com.klzw.common.auth.exception.AuthException;
import com.klzw.common.auth.util.JwtUtils;
import com.klzw.common.auth.util.PasswordUtils;
import com.klzw.common.core.exception.BusinessException;
import com.klzw.service.user.dto.RefreshTokenDTO;
import com.klzw.service.user.dto.UserLoginDTO;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.service.AuthService;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.CaptchaVO;
import com.klzw.service.user.vo.LoginVO;
import com.klzw.service.user.vo.UserVO;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordUtils passwordUtils;
    private final JwtUtils jwtUtils;

    private static final Long TOKEN_EXPIRE_TIME = 7200000L;

    @Override
    public LoginVO login(UserLoginDTO dto) {
        User user = userService.getByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        if (!passwordUtils.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(400, "账号已被禁用");
        }

        String accessToken = jwtUtils.generateToken(Long.parseLong(user.getId().hashCode() + ""), user.getUsername());
        String refreshToken = jwtUtils.generateToken(Long.parseLong(user.getId().hashCode() + ""), user.getUsername() + ":refresh");

        UserVO userVO = userService.getUserVOById(user.getId());

        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setTokenType("Bearer");
        loginVO.setExpiresIn(TOKEN_EXPIRE_TIME / 1000);
        loginVO.setUser(userVO);

        return loginVO;
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        jwtUtils.addToBlacklist(token);
    }

    @Override
    public LoginVO refreshToken(RefreshTokenDTO dto) {
        String refreshToken = dto.getRefreshToken();
        if (refreshToken == null) {
            throw new AuthException(AuthResultCode.TOKEN_MISSING, "刷新令牌不能为空");
        }

        try {
            Claims claims = jwtUtils.parseToken(refreshToken);
            String username = claims.getSubject();
            if (username != null && username.endsWith(":refresh")) {
                username = username.substring(0, username.length() - 8);
            }

            User user = userService.getByUsername(username);
            if (user == null) {
                throw new AuthException(AuthResultCode.USER_NOT_FOUND, "用户不存在");
            }

            String newAccessToken = jwtUtils.generateToken(Long.parseLong(user.getId().hashCode() + ""), user.getUsername());
            String newRefreshToken = jwtUtils.generateToken(Long.parseLong(user.getId().hashCode() + ""), user.getUsername() + ":refresh");

            UserVO userVO = userService.getUserVOById(user.getId());

            LoginVO loginVO = new LoginVO();
            loginVO.setAccessToken(newAccessToken);
            loginVO.setRefreshToken(newRefreshToken);
            loginVO.setTokenType("Bearer");
            loginVO.setExpiresIn(TOKEN_EXPIRE_TIME / 1000);
            loginVO.setUser(userVO);

            return loginVO;
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException(AuthResultCode.TOKEN_INVALID, "刷新令牌无效或已过期");
        }
    }

    @Override
    public CaptchaVO generateCaptcha() {
        String captchaKey = UUID.randomUUID().toString();
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            captcha.append((int) (Math.random() * 10));
        }
        String captchaCode = captcha.toString();
        
        String svgTemplate = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"120\" height=\"40\">" +
                "<rect width=\"100%\" height=\"100%\" fill=\"#f0f0f0\"/>" +
                "<text x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\" " +
                "font-size=\"24\" font-weight=\"bold\" fill=\"#333\">" + captchaCode + "</text>" +
                "</svg>";
        String captchaImage = "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svgTemplate.getBytes());

        CaptchaVO vo = new CaptchaVO();
        vo.setCaptchaKey(captchaKey);
        vo.setCaptchaImage(captchaImage);
        return vo;
    }
}
