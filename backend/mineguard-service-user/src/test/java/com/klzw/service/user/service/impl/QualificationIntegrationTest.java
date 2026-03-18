package com.klzw.service.user.service.impl;

import com.klzw.service.user.AbstractIntegrationTest;
import com.klzw.service.user.dto.CertVerifyDTO;
import com.klzw.service.user.dto.IdCardVerifyDTO;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.service.QualificationService;
import com.klzw.service.user.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QualificationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private QualificationService qualificationService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private com.klzw.common.auth.util.PasswordUtils passwordUtils;

    private Long testUserId;

    private void cleanTestUsers() {
        String[] usernames = {"qualification_test_user_1", "qualification_test_user_2", "qualification_test_user_3"};
        for (String username : usernames) {
            userMapper.restoreByUsername(username);
            userMapper.physicallyDeleteByUsername(username);
        }
    }

    @BeforeEach
    void setUp() {
        cleanTestUsers();
        User user = new User();
        user.setUsername("qualification_test_user_1");
        user.setPassword(passwordUtils.encode("Test123456"));
        user.setRealName("测试用户");
        user.setPhone("13800138001");
        user.setStatus(1);
        userService.createUser(user);
        testUserId = user.getId();
    }

    @Test
    @Order(1)
    @DisplayName("集成测试 - 身份证验证成功")
    void testVerifyIdCard_Success() {
        IdCardVerifyDTO dto = new IdCardVerifyDTO();
        dto.setUserId(testUserId);
        dto.setRealName("测试用户");
        dto.setIdCard("110101199001011234");
        dto.setIdCardFrontBase64("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

        boolean result = qualificationService.verifyIdCard(dto);

        assertTrue(result);
        assertTrue(qualificationService.checkIdCardVerified(testUserId));
    }

    @Test
    @Order(2)
    @DisplayName("集成测试 - 驾驶证上传成功")
    void testUploadDriverCert_Success() {
        IdCardVerifyDTO idCardDto = new IdCardVerifyDTO();
        idCardDto.setUserId(testUserId);
        idCardDto.setRealName("测试用户");
        idCardDto.setIdCard("110101199001011234");
        idCardDto.setIdCardFrontBase64("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");
        qualificationService.verifyIdCard(idCardDto);

        CertVerifyDTO dto = new CertVerifyDTO();
        dto.setUserId(testUserId);
        dto.setCertNumber("JZ123456789");
        dto.setDrivingLicenseBase64("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

        boolean result = qualificationService.uploadDriverCert(dto);

        assertTrue(result);
    }

    @Test
    @Order(3)
    @DisplayName("集成测试 - 身份证验证 - 用户不存在")
    void testVerifyIdCard_UserNotFound() {
        IdCardVerifyDTO dto = new IdCardVerifyDTO();
        dto.setUserId(99999L);
        dto.setRealName("测试用户");
        dto.setIdCard("110101199001011234");
        dto.setIdCardFrontBase64("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

        assertThrows(Exception.class, () -> qualificationService.verifyIdCard(dto));
    }
}
