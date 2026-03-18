package com.klzw.service.user.service.impl;

import com.klzw.common.auth.constant.AuthResultCode;
import com.klzw.common.auth.exception.AuthException;
import com.klzw.common.auth.util.JwtUtils;
import com.klzw.common.auth.util.PasswordUtils;
import com.klzw.common.core.enums.UserStatusEnum;
import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.service.user.dto.RefreshTokenDTO;
import com.klzw.service.user.dto.ResetPasswordDTO;
import com.klzw.service.user.dto.UserLoginDTO;
import com.klzw.service.user.dto.UserRegisterDTO;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.mapper.RoleMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.CaptchaVO;
import com.klzw.service.user.vo.SmsCodeVO;
import com.klzw.service.user.vo.UserVO;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private PasswordUtils passwordUtils;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private UserVO testUserVO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setPhone("13800138000");
        testUser.setEmail("test@example.com");
        testUser.setStatus(UserStatusEnum.ENABLED.getValue());
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());

        testUserVO = new UserVO();
        testUserVO.setId("1");
        testUserVO.setUsername("testuser");
        testUserVO.setRealName("测试用户");
        testUserVO.setPhone("13800138000");
        testUserVO.setEmail("test@example.com");
        testUserVO.setStatus(UserStatusEnum.ENABLED.getValue());
    }

    @Test
    @DisplayName("用户注册 - 成功")
    void register_Success() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("Test123");
        dto.setRealName("新用户");
        dto.setPhone("13800138001");

        UserVO newUserVO = new UserVO();
        newUserVO.setId("2");
        newUserVO.setUsername("newuser");
        newUserVO.setPhone("13800138001");

        when(userService.getByUsername("newuser")).thenReturn(null);
        when(userService.getByPhone("13800138001")).thenReturn(null);
        when(passwordUtils.encode("Test123")).thenReturn("encodedPassword");
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return null;
        }).when(userService).createUser(any(User.class));
        when(userService.getUserVOById(2L)).thenReturn(newUserVO);
        when(jwtUtils.generateToken(2L, "newuser")).thenReturn("accessToken");
        when(jwtUtils.generateToken(2L, "newuser:refresh")).thenReturn("refreshToken");

        UserVO result = authService.register(dto);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(userService).createUser(any(User.class));
    }

    @Test
    @DisplayName("用户登录 - 成功")
    void login_Success() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("Test123");

        when(userService.getByUsername("testuser")).thenReturn(testUser);
        when(passwordUtils.matches("Test123", "encodedPassword")).thenReturn(true);
        when(userService.getUserVOById(1L)).thenReturn(testUserVO);
        when(jwtUtils.generateToken(1L, "testuser")).thenReturn("accessToken");
        when(jwtUtils.generateToken(1L, "testuser:refresh")).thenReturn("refreshToken");

        UserVO result = authService.login(dto);

        assertNotNull(result);
        assertEquals("accessToken", result.getToken());
        assertEquals("refreshToken", result.getRefreshToken());
    }

    @Test
    @DisplayName("用户登录 - 用户名不存在")
    void login_UserNotFound() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername("nonexistent");
        dto.setPassword("Test123");

        when(userService.getByUsername("nonexistent")).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(dto));
        assertTrue(exception.getMessage().contains("用户名或密码错误"));
    }

    @Test
    @DisplayName("用户登录 - 密码错误")
    void login_WrongPassword() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("WrongPassword");

        when(userService.getByUsername("testuser")).thenReturn(testUser);
        when(passwordUtils.matches("WrongPassword", "encodedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(dto));
        assertTrue(exception.getMessage().contains("用户名或密码错误"));
    }

    @Test
    @DisplayName("用户登出 - 成功")
    void logout_Success() {
        String token = "Bearer accessToken123";

        doNothing().when(jwtUtils).addToBlacklist("accessToken123");

        assertDoesNotThrow(() -> authService.logout(token));
        verify(jwtUtils).addToBlacklist("accessToken123");
    }

    @Test
    @DisplayName("用户登出 - 无Bearer前缀")
    void logout_WithoutBearer() {
        String token = "accessToken123";

        doNothing().when(jwtUtils).addToBlacklist("accessToken123");

        assertDoesNotThrow(() -> authService.logout(token));
        verify(jwtUtils).addToBlacklist("accessToken123");
    }

    @Test
    @DisplayName("刷新Token - 成功")
    void refreshToken_Success() throws Exception {
        RefreshTokenDTO dto = new RefreshTokenDTO();
        dto.setRefreshToken("refreshToken123");

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("testuser:refresh")
                .build();

        when(jwtUtils.parseToken("refreshToken123")).thenReturn(claims);
        when(userService.getByUsername("testuser")).thenReturn(testUser);
        when(userService.getUserVOById(1L)).thenReturn(testUserVO);
        when(jwtUtils.generateToken(1L, "testuser")).thenReturn("newAccessToken");
        when(jwtUtils.generateToken(1L, "testuser:refresh")).thenReturn("newRefreshToken");

        UserVO result = authService.refreshToken(dto);

        assertNotNull(result);
        assertEquals("newAccessToken", result.getToken());
        assertEquals("newRefreshToken", result.getRefreshToken());
    }

    @Test
    @DisplayName("刷新Token - Token为空")
    void refreshToken_EmptyToken() {
        RefreshTokenDTO dto = new RefreshTokenDTO();
        dto.setRefreshToken(null);

        AuthException exception = assertThrows(AuthException.class, () -> authService.refreshToken(dto));
        assertEquals(AuthResultCode.TOKEN_MISSING.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("刷新Token - 用户不存在")
    void refreshToken_UserNotFound() throws Exception {
        RefreshTokenDTO dto = new RefreshTokenDTO();
        dto.setRefreshToken("refreshToken123");

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("testuser:refresh")
                .build();

        when(jwtUtils.parseToken("refreshToken123")).thenReturn(claims);
        when(userService.getByUsername("testuser")).thenReturn(null);

        AuthException exception = assertThrows(AuthException.class, () -> authService.refreshToken(dto));
        assertEquals(AuthResultCode.USER_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("生成验证码 - 成功")
    void generateCaptcha_Success() {
        CaptchaVO result = authService.generateCaptcha();

        assertNotNull(result);
        assertNotNull(result.getCaptchaKey());
        assertNotNull(result.getCaptchaImage());
        assertTrue(result.getCaptchaImage().startsWith("data:image/svg+xml;base64,"));
    }

    @Test
    @DisplayName("发送短信验证码 - 成功")
    void sendSmsCode_Success() {
        SmsCodeVO result = authService.sendSmsCode("13800138000");

        assertNotNull(result);
        assertNotNull(result.getSmsId());
        verify(redisCacheService).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("验证短信验证码 - 默认返回false")
    void verifySmsCode_Default() {
        boolean result = authService.verifySmsCode("13800138000", "123456");

        assertFalse(result);
    }

    @Test
    @DisplayName("通过手机号重置密码 - 成功")
    void resetPasswordByPhone_Success() {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setPhone("13800138000");
        dto.setNewPassword("NewTest123");

        when(userService.getByPhone("13800138000")).thenReturn(testUser);
        when(passwordUtils.encode("NewTest123")).thenReturn("newEncodedPassword");
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> authService.resetPasswordByPhone(dto));
        verify(userMapper).updateById(any(User.class));
    }
}
