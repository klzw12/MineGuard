package com.klzw.service.user.service.impl;

import com.klzw.common.auth.constant.AuthResultCode;
import com.klzw.common.auth.exception.AuthException;
import com.klzw.common.auth.util.JwtUtils;
import com.klzw.common.auth.util.PasswordUtils;
import com.klzw.common.core.enums.UserStatusEnum;
import com.klzw.common.file.service.OcrService;
import com.klzw.common.file.service.StorageService;
import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.service.user.dto.AdminVerifyDTO;
import com.klzw.service.user.dto.RefreshTokenDTO;
import com.klzw.service.user.dto.UserLoginDTO;
import com.klzw.service.user.dto.UserRegisterDTO;
import com.klzw.service.user.dto.ResetPasswordDTO;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.exception.UserException;
import com.klzw.service.user.constant.UserResultCode;
import com.klzw.service.user.mapper.RoleMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.service.AuthService;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.CaptchaVO;
import com.klzw.service.user.vo.SmsCodeVO;
import com.klzw.service.user.vo.UserVO;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Long ADMIN_ROLE_ID = 1L;
    private static final Long TOKEN_EXPIRE_TIME = 7200000L;
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final long SMS_CODE_EXPIRE = 5;

    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordUtils passwordUtils;
    private final JwtUtils jwtUtils;
    private final RedisCacheService redisCacheService;
    private final StorageService storageService;
    private final OcrService ocrService;

    @Override
    public UserVO register(UserRegisterDTO dto) {
        User existUser = userService.getByUsername(dto.getUsername());
        if (existUser != null) {
            throw new UserException(UserResultCode.USERNAME_EXISTS);
        }

        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            User userByPhone = userService.getByPhone(dto.getPhone());
            if (userByPhone != null) {
                throw new UserException(UserResultCode.PHONE_EXISTS);
            }
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordUtils.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(UserStatusEnum.ENABLED.getValue());

        userService.createUser(user);

        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername(dto.getUsername());
        loginDTO.setPassword(dto.getPassword());
        
        UserVO userVO = userService.getUserVOById(user.getId());
        String accessToken = jwtUtils.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtils.generateToken(user.getId(), user.getUsername() + ":refresh");
        userVO.setToken(accessToken);
        userVO.setRefreshToken(refreshToken);
        userVO.setExpiresIn(TOKEN_EXPIRE_TIME);
        
        return userVO;
    }

    @Override
    public UserVO login(UserLoginDTO dto) {
        User user = userService.getByUsername(dto.getUsername());
        if (user == null) {
            throw new UserException(UserResultCode.PASSWORD_ERROR, "用户名或密码错误");
        }

        if (!passwordUtils.matches(dto.getPassword(), user.getPassword())) {
            throw new UserException(UserResultCode.PASSWORD_ERROR, "用户名或密码错误");
        }

        // 检查用户状态，管理员即使被禁用也允许登录
        if (user.getStatus() != UserStatusEnum.ENABLED.getValue() && !ADMIN_ROLE_ID.equals(user.getRoleId())) {
            throw new UserException(UserResultCode.USER_DISABLED);
        }

        String accessToken = jwtUtils.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtils.generateToken(user.getId(), user.getUsername() + ":refresh");

        UserVO userVO = userService.getUserVOById(user.getId());
        if (userVO == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND, "用户信息获取失败");
        }
        userVO.setToken(accessToken);
        userVO.setRefreshToken(refreshToken);
        userVO.setExpiresIn(TOKEN_EXPIRE_TIME);

        return userVO;
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        jwtUtils.addToBlacklist(token);
    }

    @Override
    public UserVO refreshToken(RefreshTokenDTO dto) {
        String refreshToken = dto.getRefreshToken();
        if (refreshToken == null) {
            throw new AuthException(AuthResultCode.TOKEN_MISSING, "刷新令牌不能为空");
        }

        try {
            JWTClaimsSet claims = jwtUtils.parseToken(refreshToken);
            String username = claims.getSubject();
            if (username != null && username.endsWith(":refresh")) {
                username = username.substring(0, username.length() - 8);
            }

            User user = userService.getByUsername(username);
            if (user == null) {
                throw new AuthException(AuthResultCode.USER_NOT_FOUND, "用户不存在");
            }

            String newAccessToken = jwtUtils.generateToken(user.getId(), user.getUsername());
            String newRefreshToken = jwtUtils.generateToken(user.getId(), user.getUsername() + ":refresh");

            UserVO userVO = userService.getUserVOById(user.getId());
            userVO.setToken(newAccessToken);
            userVO.setRefreshToken(newRefreshToken);
            userVO.setExpiresIn(TOKEN_EXPIRE_TIME);

            return userVO;
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

    @Override
    public SmsCodeVO sendSmsCode(String phone) {
        String code = generateSmsCode(6);
        String smsId = UUID.randomUUID().toString().replace("-", "");

        redisCacheService.set(SMS_CODE_PREFIX + smsId, phone + ":" + code, SMS_CODE_EXPIRE, TimeUnit.MINUTES);

        log.info("发送短信验证码，手机号：{}，验证码：{}", phone, code);

        SmsCodeVO vo = new SmsCodeVO();
        vo.setSmsId(smsId);
        return vo;
    }

    @Override
    public boolean verifySmsCode(String phone, String code) {
        return false;
    }

    @Override
    public void resetPasswordByPhone(ResetPasswordDTO dto) {
        User user = userService.getByPhone(dto.getPhone());
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND);
        }

        user.setPassword(passwordUtils.encode(dto.getNewPassword()));
        userMapper.updateById(user);

        log.info("重置密码成功，用户ID：{}", user.getId());
    }

    @Override
    @Transactional
    public UserVO verifyAdmin(AdminVerifyDTO dto) {
        log.info("管理员认证，用户ID：{}", dto.getUserId());
        
        User user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND);
        }
        
        if (!ADMIN_ROLE_ID.equals(user.getRoleId())) {
            throw new UserException(UserResultCode.PARAM_ERROR, "非管理员用户无法进行管理员认证");
        }
        
        String idCardFrontUrl = uploadImage(dto.getIdCardFrontBase64(), "admin_verify");
        Map<String, String> idCardInfo = ocrService.parseIdCard(idCardFrontUrl);
        
        if (idCardInfo == null || idCardInfo.isEmpty()) {
            throw new UserException(UserResultCode.ID_CARD_INVALID, "身份证识别失败");
        }
        
        String actualName = idCardInfo.get("name");
        String actualIdCard = idCardInfo.get("idNumber");
        if (actualIdCard == null || actualIdCard.isEmpty()) {
            throw new UserException(UserResultCode.ID_CARD_INVALID, "身份证识别失败：无法识别身份证号");
        }
        
        if (actualName == null || actualName.isEmpty()) {
            throw new UserException(UserResultCode.ID_CARD_INVALID, "身份证识别失败：无法识别姓名");
        }
        
        if (dto.getRealName() != null && !dto.getRealName().isEmpty() && !actualName.equals(dto.getRealName())) {
            throw new UserException(UserResultCode.ID_CARD_INVALID, "身份证姓名与提交姓名不一致");
        }
        
        user.setRealName(actualName);
        user.setPhone(dto.getPhone());
        user.setStatus(UserStatusEnum.ENABLED.getValue());
        userMapper.updateById(user);
        
        userService.clearUserCache(user.getId());
        
        log.info("管理员认证成功，用户ID：{}", dto.getUserId());
        
        return userService.getUserVOById(user.getId());
    }
    
    private String uploadImage(String base64Image, String folder) {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new UserException(UserResultCode.PARAM_ERROR, "图片不能为空");
        }
        
        try {
            String base64Data = base64Image;
            if (base64Image.contains(",")) {
                base64Data = base64Image.split(",")[1];
            }
            
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
            
            String fileName = folder + "/" + System.currentTimeMillis() + ".jpg";
            return storageService.upload(inputStream, fileName, "image/jpeg");
            
        } catch (Exception e) {
            log.error("图片上传失败", e);
            throw new UserException(UserResultCode.QUALIFICATION_VERIFY_FAILED, "图片上传失败");
        }
    }

    private String generateSmsCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append((int) (Math.random() * 10));
        }
        return code.toString();
    }
}
