package com.klzw.service.user.service.impl;

import com.klzw.service.user.AbstractIntegrationTest;
import com.klzw.service.user.dto.IdCardVerifyDTO;
import com.klzw.service.user.dto.UserLoginDTO;
import com.klzw.service.user.dto.UserRegisterDTO;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.service.AuthService;
import com.klzw.service.user.service.QualificationService;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.UserVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private QualificationService qualificationService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private com.klzw.common.auth.util.PasswordUtils passwordUtils;

    private static Long testUserId;
    private static int phoneCounter = 0;

    private static synchronized String getUniquePhone() {
        return "139" + String.format("%08d", ++phoneCounter);
    }

    private void cleanTestUsers() {
        String[] usernames = {"integration_test_user", "login_test_user", "idcard_test_user", "check_idcard_user", "refresh_test_user"};
        for (String username : usernames) {
            userMapper.restoreByUsername(username);
            userMapper.physicallyDeleteByUsername(username);
        }
    }

    @BeforeEach
    void setUp() {
        cleanTestUsers();
    }

    @Test
    @Order(1)
    @DisplayName("集成测试 - 用户注册流程")
    void testUserRegister() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("integration_test_user");
        dto.setPassword("Test123456");
        dto.setRealName("测试用户");
        dto.setPhone(getUniquePhone());

        UserVO result = authService.register(dto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("integration_test_user", result.getUsername());
        assertNotNull(result.getToken());

        testUserId = Long.parseLong(result.getId());
    }

    @Test
    @Order(2)
    @DisplayName("集成测试 - 用户登录流程")
    void testUserLogin() {
        User user = new User();
        user.setUsername("login_test_user");
        user.setPassword(passwordUtils.encode("Test123456"));
        user.setRealName("登录测试用户");
        user.setPhone(getUniquePhone());
        user.setStatus(1);
        userService.createUser(user);

        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername("login_test_user");
        dto.setPassword("Test123456");

        UserVO result = authService.login(dto);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertNotNull(result.getRefreshToken());
    }

    @Test
    @Order(3)
    @DisplayName("集成测试 - 身份证验证流程")
    void testIdCardVerification() {
        User user = new User();
        user.setUsername("idcard_test_user");
        user.setPassword("Test123456");
        user.setPhone(getUniquePhone());
        userService.createUser(user);

        IdCardVerifyDTO dto = new IdCardVerifyDTO();
        dto.setUserId(user.getId());
        dto.setRealName("测试用户");
        dto.setIdCard("110101199001011234");
        dto.setIdCardFrontBase64("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

        boolean result = qualificationService.verifyIdCard(dto);
        assertTrue(result);
    }

    @Test
    @Order(4)
    @DisplayName("集成测试 - 检查身份证验证状态")
    void testCheckIdCardVerified() {
        User user = new User();
        user.setUsername("check_idcard_user");
        user.setPassword("Test123456");
        user.setPhone(getUniquePhone());
        userService.createUser(user);

        boolean result = qualificationService.checkIdCardVerified(user.getId());
        assertFalse(result);
    }

    @Test
    @Order(5)
    @DisplayName("集成测试 - Token刷新流程")
    void testTokenRefresh() {
        User user = new User();
        user.setUsername("refresh_test_user");
        user.setPassword(passwordUtils.encode("Test123456"));
        user.setRealName("Token刷新测试用户");
        user.setPhone(getUniquePhone());
        user.setStatus(1);
        userService.createUser(user);

        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("refresh_test_user");
        loginDTO.setPassword("Test123456");

        UserVO loginResult = authService.login(loginDTO);
        assertNotNull(loginResult.getRefreshToken());

        var refreshDTO = new com.klzw.service.user.dto.RefreshTokenDTO();
        refreshDTO.setRefreshToken(loginResult.getRefreshToken());
        UserVO refreshResult = authService.refreshToken(refreshDTO);
        assertNotNull(refreshResult.getToken());
    }
}
