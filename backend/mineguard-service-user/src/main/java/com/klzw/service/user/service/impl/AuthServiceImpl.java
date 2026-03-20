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
import com.klzw.service.user.enums.SmsScene;
import com.klzw.service.user.exception.UserException;
import com.klzw.service.user.constant.UserResultCode;
import com.klzw.service.user.mapper.RoleMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.service.AuthService;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.service.sms.SmsService;
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
    private final SmsService smsService;

    @Override
    public UserVO register(UserRegisterDTO dto) {
        User existUser = userService.getByUsername(dto.getUsername());
        if (existUser != null) {
            throw new UserException(UserResultCode.USERNAME_EXISTS);
        }

        User userByPhone = userService.getByPhone(dto.getPhone());
        if (userByPhone != null) {
            throw new UserException(UserResultCode.PHONE_EXISTS);
        }

        boolean smsVerified = smsService.verifySmsCode(dto.getPhone(), dto.getSmsCode());
        if (!smsVerified) {
            throw new UserException(UserResultCode.SMS_VERIFY_FAILED, "短信验证码错误或已过期");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordUtils.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(UserStatusEnum.ENABLED.getValue());

        userService.createUser(user);

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
        return sendSmsCode(phone, SmsScene.REGISTER);
    }

    @Override
    public SmsCodeVO sendSmsCode(String phone, SmsScene scene) {
        boolean result = smsService.sendSmsCode(phone, scene);
        if (!result) {
            throw new UserException(UserResultCode.SMS_SEND_FAILED);
        }

        log.info("发送{}验证码，手机号：{}", scene.getDescription(), phone);

        SmsCodeVO vo = new SmsCodeVO();
        return vo;
    }

    @Override
    public boolean verifySmsCode(String phone, String code) {
        return smsService.verifySmsCode(phone, code);
    }

    @Override
    public void resetPasswordByPhone(ResetPasswordDTO dto) {
        boolean smsVerified = smsService.verifySmsCode(dto.getPhone(), dto.getSmsCode());
        if (!smsVerified) {
            throw new UserException(UserResultCode.SMS_VERIFY_FAILED, "短信验证码错误或已过期");
        }

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
        
        Long userId = Long.parseLong(dto.getUserId());
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND);
        }
        
        if (!ADMIN_ROLE_ID.equals(user.getRoleId())) {
            throw new UserException(UserResultCode.PARAM_ERROR, "非管理员用户无法进行管理员认证");
        }
        
        // 先识别身份证，再上传到OSS
        byte[] frontImageBytes = decodeBase64Image(dto.getIdCardFrontBase64());
        String ocrResult = ocrService.recognizeIdCard(frontImageBytes);
        Map<String, String> idCardInfo = ocrService.parseIdCard(ocrResult);
        
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
        
        // 验证姓名：如果用户已有姓名，验证是否一致
        if (user.getRealName() != null && !user.getRealName().isEmpty()) {
            if (!actualName.equals(user.getRealName())) {
                throw new UserException(UserResultCode.ID_CARD_INVALID, "身份证姓名与账号姓名不一致");
            }
        } else if (dto.getRealName() != null && !dto.getRealName().isEmpty()) {
            // 如果用户没有姓名但传入了姓名，验证是否一致
            if (!actualName.equals(dto.getRealName())) {
                throw new UserException(UserResultCode.ID_CARD_INVALID, "身份证姓名与提交姓名不一致");
            }
            user.setRealName(dto.getRealName());
        } else {
            // 使用 OCR 识别的姓名
            user.setRealName(actualName);
        }
        
        // 验证身份证号：如果用户传入了身份证号，验证是否一致
        if (dto.getIdCard() != null && !dto.getIdCard().isEmpty()) {
            if (!actualIdCard.equals(dto.getIdCard())) {
                throw new UserException(UserResultCode.ID_CARD_INVALID, "身份证号与提交的不一致");
            }
        }
        
        // OCR识别成功后，上传图片到OSS
        String idCardFrontUrl = uploadImageFromBytes(frontImageBytes, "admin_verify");
        String idCardBackUrl = null;
        if (dto.getIdCardBackBase64() != null && !dto.getIdCardBackBase64().isEmpty()) {
            byte[] backImageBytes = decodeBase64Image(dto.getIdCardBackBase64());
            idCardBackUrl = uploadImageFromBytes(backImageBytes, "admin_verify");
        }
        
        user.setIdCard(actualIdCard);
        user.setIdCardFrontUrl(idCardFrontUrl);
        if (idCardBackUrl != null) {
            user.setIdCardBackUrl(idCardBackUrl);
        }
        user.setStatus(UserStatusEnum.ENABLED.getValue());
        userMapper.updateById(user);
        
        userService.clearUserCache(user.getId());
        
        log.info("管理员认证成功，用户ID：{}", dto.getUserId());
        
        UserVO userVO = userService.getUserVOById(user.getId());
        String accessToken = jwtUtils.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtils.generateToken(user.getId(), user.getUsername() + ":refresh");
        userVO.setToken(accessToken);
        userVO.setRefreshToken(refreshToken);
        userVO.setExpiresIn(TOKEN_EXPIRE_TIME);
        
        return userVO;
    }
    
    private String uploadImage(String base64Image, String folder) {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new UserException(UserResultCode.PARAM_ERROR, "图片不能为空");
        }
        
        try {
            byte[] imageBytes = decodeBase64Image(base64Image);
            return uploadImageFromBytes(imageBytes, folder);
            
        } catch (Exception e) {
            log.error("图片上传失败", e);
            throw new UserException(UserResultCode.QUALIFICATION_VERIFY_FAILED, "图片上传失败");
        }
    }
    
    private byte[] decodeBase64Image(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new UserException(UserResultCode.PARAM_ERROR, "图片不能为空");
        }
        
        try {
            String base64Data = base64Image;
            if (base64Image.contains(",")) {
                base64Data = base64Image.split(",")[1];
            }
            return Base64.getDecoder().decode(base64Data);
        } catch (Exception e) {
            log.error("Base64解码失败", e);
            throw new UserException(UserResultCode.PARAM_ERROR, "图片格式错误");
        }
    }
    
    private String uploadImageFromBytes(byte[] imageBytes, String folder) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new UserException(UserResultCode.PARAM_ERROR, "图片不能为空");
        }
        
        try {
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
