package com.klzw.service.user.controller;

import com.klzw.common.core.enums.UserStatusEnum;
import com.klzw.service.user.dto.ResetPasswordDTO;
import com.klzw.service.user.dto.UserLoginDTO;
import com.klzw.service.user.dto.UserRegisterDTO;
import com.klzw.service.user.service.AuthService;
import com.klzw.service.user.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private UserVO testUserVO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        testUserVO = new UserVO();
        testUserVO.setId("1");
        testUserVO.setUsername("testuser");
        testUserVO.setRealName("测试用户");
        testUserVO.setPhone("13800138000");
        testUserVO.setEmail("test@example.com");
        testUserVO.setStatus(UserStatusEnum.ENABLED.getValue());
        testUserVO.setToken("accessToken123");
        testUserVO.setRefreshToken("refreshToken456");
        testUserVO.setExpiresIn(7200L);
    }

    @Test
    @DisplayName("用户注册 - 成功")
    void register_Success() throws Exception {
        UserVO registeredUser = new UserVO();
        registeredUser.setId("2");
        registeredUser.setUsername("newuser");
        registeredUser.setToken("newAccessToken");

        when(authService.register(any(UserRegisterDTO.class))).thenReturn(registeredUser);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newuser\",\"password\":\"Password123\",\"realName\":\"新用户\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andExpect(jsonPath("$.data.username").value("newuser"));

        verify(authService).register(any(UserRegisterDTO.class));
    }

    @Test
    @DisplayName("用户登录 - 成功")
    void login_Success() throws Exception {
        when(authService.login(any(UserLoginDTO.class))).thenReturn(testUserVO);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"password\":\"Password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.token").value("accessToken123"));

        verify(authService).login(any(UserLoginDTO.class));
    }

    @Test
    @DisplayName("通过手机号重置密码 - 成功")
    void resetPasswordByPhone_Success() throws Exception {
        doNothing().when(authService).resetPasswordByPhone(any(ResetPasswordDTO.class));

        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phone\":\"13800138000\",\"code\":\"123456\",\"newPassword\":\"NewPassword456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("密码重置成功"));

        verify(authService).resetPasswordByPhone(any(ResetPasswordDTO.class));
    }

    @Test
    @DisplayName("用户登录 - 验证Token信息")
    void login_VerifyTokenInfo() throws Exception {
        UserVO userWithToken = new UserVO();
        userWithToken.setId("1");
        userWithToken.setUsername("testuser");
        userWithToken.setToken("testAccessToken");
        userWithToken.setRefreshToken("testRefreshToken");
        userWithToken.setExpiresIn(3600L);

        when(authService.login(any(UserLoginDTO.class))).thenReturn(userWithToken);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"password\":\"Password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("testAccessToken"))
                .andExpect(jsonPath("$.data.refreshToken").value("testRefreshToken"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600));

        verify(authService).login(any(UserLoginDTO.class));
    }
}
