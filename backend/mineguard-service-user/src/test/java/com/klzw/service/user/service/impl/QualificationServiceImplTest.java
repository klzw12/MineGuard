package com.klzw.service.user.service.impl;

import com.klzw.common.auth.util.PasswordUtils;
import com.klzw.common.core.enums.UserStatusEnum;
import com.klzw.common.file.service.OcrService;
import com.klzw.common.file.service.StorageService;
import com.klzw.service.user.dto.CertVerifyDTO;
import com.klzw.service.user.dto.IdCardVerifyDTO;
import com.klzw.service.user.entity.Driver;
import com.klzw.service.user.entity.Repairman;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.entity.SafetyOfficer;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.exception.UserException;
import com.klzw.service.user.mapper.DriverMapper;
import com.klzw.service.user.mapper.RepairmanMapper;
import com.klzw.service.user.mapper.RoleMapper;
import com.klzw.service.user.mapper.SafetyOfficerMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.service.RoleChangeApplyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QualificationServiceImplTest {

    @Mock
    private StorageService storageService;

    @Mock
    private OcrService ocrService;

    @Mock
    private DriverMapper driverMapper;

    @Mock
    private SafetyOfficerMapper safetyOfficerMapper;

    @Mock
    private RepairmanMapper repairmanMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private RoleChangeApplyService roleChangeApplyService;

    @Mock
    private PasswordUtils passwordUtils;

    @InjectMocks
    private QualificationServiceImpl qualificationService;

    private User testUser;
    private IdCardVerifyDTO idCardVerifyDTO;
    private CertVerifyDTO certVerifyDTO;
    private Map<String, String> idCardInfo;
    private Map<String, String> licenseInfo;
    private Role driverRole;
    private Role safetyOfficerRole;
    private Role repairmanRole;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("张三");
        testUser.setPhone("13800138000");
        testUser.setStatus(UserStatusEnum.ENABLED.getValue());

        idCardVerifyDTO = new IdCardVerifyDTO();
        idCardVerifyDTO.setUserId(1L);
        idCardVerifyDTO.setRealName("张三");
        idCardVerifyDTO.setIdCard("110101199001011234");
        idCardVerifyDTO.setIdCardFrontBase64("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

        certVerifyDTO = new CertVerifyDTO();
        certVerifyDTO.setUserId(1L);
        certVerifyDTO.setCertNumber("JZ123456789");
        certVerifyDTO.setDrivingLicenseBase64("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

        idCardInfo = new HashMap<>();
        idCardInfo.put("name", "张三");
        idCardInfo.put("idNumber", "110101199001011234");
        idCardInfo.put("gender", "男");
        idCardInfo.put("nation", "汉");
        idCardInfo.put("birth", "1990年01月01日");
        idCardInfo.put("address", "北京市东城区测试路123号");

        licenseInfo = new HashMap<>();
        licenseInfo.put("name", "张三");
        licenseInfo.put("准驾车型", "C1");
        licenseInfo.put("gender", "男");
        licenseInfo.put("address", "北京市东城区测试路123号");
        licenseInfo.put("birth", "1990年01月01日");
        licenseInfo.put("firstIssueDate", "2015年01月01日");
        licenseInfo.put("validPeriod", "2021年01月01日至2027年01月01日");
        licenseInfo.put("licenseNumber", "110101199001011234");

        driverRole = new Role();
        driverRole.setId(1L);
        driverRole.setRoleCode("ROLE_DRIVER");
        driverRole.setRoleName("司机");

        safetyOfficerRole = new Role();
        safetyOfficerRole.setId(2L);
        safetyOfficerRole.setRoleCode("ROLE_SAFETY_OFFICER");
        safetyOfficerRole.setRoleName("安全员");

        repairmanRole = new Role();
        repairmanRole.setId(3L);
        repairmanRole.setRoleCode("ROLE_REPAIRMAN");
        repairmanRole.setRoleName("维修员");

        when(passwordUtils.encodeIdCard(anyString())).thenReturn("encrypted_id_card_value");
    }

    @Test
    @DisplayName("身份证验证 - 成功")
    void verifyIdCard_Success() throws Exception {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(storageService.upload(any(InputStream.class), anyString(), anyString())).thenReturn("http://example.com/idcard.jpg");
        when(ocrService.parseIdCard(anyString())).thenReturn(idCardInfo);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        boolean result = qualificationService.verifyIdCard(idCardVerifyDTO);

        assertTrue(result);
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    @DisplayName("身份证验证 - 用户不存在")
    void verifyIdCard_UserNotFound() {
        when(userMapper.selectById(1L)).thenReturn(null);

        assertThrows(UserException.class, () -> qualificationService.verifyIdCard(idCardVerifyDTO));
    }

    @Test
    @DisplayName("身份证验证 - 姓名不匹配")
    void verifyIdCard_NameMismatch() throws Exception {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(storageService.upload(any(InputStream.class), anyString(), anyString())).thenReturn("http://example.com/idcard.jpg");
        
        Map<String, String> wrongInfo = new HashMap<>();
        wrongInfo.put("name", "李四");
        wrongInfo.put("idNumber", "110101199001011234");
        when(ocrService.parseIdCard(anyString())).thenReturn(wrongInfo);

        assertThrows(UserException.class, () -> qualificationService.verifyIdCard(idCardVerifyDTO));
    }

    @Test
    @DisplayName("身份证验证 - 身份证号不匹配")
    void verifyIdCard_IdCardMismatch() throws Exception {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(storageService.upload(any(), anyString(), anyString())).thenReturn("http://example.com/idcard.jpg");
        
        Map<String, String> wrongInfo = new HashMap<>();
        wrongInfo.put("name", "张三");
        wrongInfo.put("idNumber", "110101199001011235");
        when(ocrService.parseIdCard(anyString())).thenReturn(wrongInfo);

        assertThrows(UserException.class, () -> qualificationService.verifyIdCard(idCardVerifyDTO));
    }

    @Test
    @DisplayName("检查身份证验证状态 - 已验证")
    void checkIdCardVerified_True() {
        testUser.setRealName("张三");
        when(userMapper.selectById(1L)).thenReturn(testUser);

        boolean result = qualificationService.checkIdCardVerified(1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("检查身份证验证状态 - 未验证")
    void checkIdCardVerified_False() {
        testUser.setRealName(null);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        boolean result = qualificationService.checkIdCardVerified(1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("上传驾驶证 - 成功")
    void uploadDriverCert_Success() throws Exception {
        testUser.setRealName("张三");
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(storageService.upload(any(), anyString(), anyString())).thenReturn("http://example.com/license.jpg");
        when(ocrService.parseDrivingLicense(anyString())).thenReturn(licenseInfo);
        when(driverMapper.selectByUserId(anyString())).thenReturn(null);
        when(driverMapper.insert(any(Driver.class))).thenReturn(1);
        when(roleMapper.selectByRoleCode("ROLE_DRIVER")).thenReturn(driverRole);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        boolean result = qualificationService.uploadDriverCert(certVerifyDTO);

        assertTrue(result);
        verify(driverMapper).insert(any(Driver.class));
    }

    @Test
    @DisplayName("上传驾驶证 - 未完成实名认证")
    void uploadDriverCert_NotIdCardVerified() {
        testUser.setRealName(null);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        assertThrows(UserException.class, () -> qualificationService.uploadDriverCert(certVerifyDTO));
    }

    @Test
    @DisplayName("上传驾驶证 - 姓名不匹配")
    void uploadDriverCert_NameMismatch() throws Exception {
        testUser.setRealName("张三");
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(storageService.upload(any(), anyString(), anyString())).thenReturn("http://example.com/license.jpg");
        
        Map<String, String> wrongLicense = new HashMap<>();
        wrongLicense.put("name", "李四");
        wrongLicense.put("准驾车型", "C1");
        when(ocrService.parseDrivingLicense(anyString())).thenReturn(wrongLicense);

        assertThrows(UserException.class, () -> qualificationService.uploadDriverCert(certVerifyDTO));
    }

    @Test
    @DisplayName("上传驾驶证 - 更新已有记录")
    void uploadDriverCert_Update() throws Exception {
        testUser.setRealName("张三");
        Driver existingDriver = new Driver();
        existingDriver.setId(1L);
        existingDriver.setUserId(1L);

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(storageService.upload(any(), anyString(), anyString())).thenReturn("http://example.com/license.jpg");
        when(ocrService.parseDrivingLicense(anyString())).thenReturn(licenseInfo);
        when(driverMapper.selectByUserId(anyString())).thenReturn(existingDriver);
        when(driverMapper.updateById(any(Driver.class))).thenReturn(1);
        when(roleMapper.selectByRoleCode("ROLE_DRIVER")).thenReturn(driverRole);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        boolean result = qualificationService.uploadDriverCert(certVerifyDTO);

        assertTrue(result);
        verify(driverMapper).updateById(any(Driver.class));
    }

    @Test
    @DisplayName("上传应急救援证 - 成功")
    void uploadSafetyOfficerCert_Success() throws Exception {
        testUser.setRealName("张三");
        certVerifyDTO.setDrivingLicenseBase64(null);
        certVerifyDTO.setEmergencyCertBase64("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

        Map<String, String> certInfo = new HashMap<>();
        certInfo.put("name", "张三");
        certInfo.put("certificateNumber", "AQ123456789");
        certInfo.put("trainingItem", "应急救援");
        certInfo.put("validPeriod", "2030-12-31");

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(storageService.upload(any(), anyString(), anyString())).thenReturn("http://example.com/cert.jpg");
        when(ocrService.parseEmergencyCert(anyString())).thenReturn(certInfo);
        when(safetyOfficerMapper.selectByUserId(anyString())).thenReturn(null);
        when(safetyOfficerMapper.insert(any(SafetyOfficer.class))).thenReturn(1);
        when(roleMapper.selectByRoleCode("ROLE_SAFETY_OFFICER")).thenReturn(safetyOfficerRole);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        boolean result = qualificationService.uploadSafetyOfficerCert(certVerifyDTO);

        assertTrue(result);
        verify(safetyOfficerMapper).insert(any(SafetyOfficer.class));
    }

    @Test
    @DisplayName("上传应急救援证 - 未完成实名认证")
    void uploadSafetyOfficerCert_NotIdCardVerified() {
        testUser.setRealName(null);
        certVerifyDTO.setDrivingLicenseBase64(null);
        certVerifyDTO.setEmergencyCertBase64("data:image/png;base64,emergency");

        when(userMapper.selectById(1L)).thenReturn(testUser);

        assertThrows(UserException.class, () -> qualificationService.uploadSafetyOfficerCert(certVerifyDTO));
    }

    @Test
    @DisplayName("上传维修资格证 - 成功")
    void uploadRepairmanCert_Success() throws Exception {
        testUser.setRealName("张三");
        certVerifyDTO.setDrivingLicenseBase64(null);
        certVerifyDTO.setRepairCertBase64("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

        Map<String, String> certInfo = new HashMap<>();
        certInfo.put("name", "张三");
        certInfo.put("certificateNumber", "WX123456789");
        certInfo.put("level", "中级");
        certInfo.put("category", "机电维修");
        certInfo.put("validUntil", "2030-12-31");
        certInfo.put("issueDate", "2020-01-01");

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(storageService.upload(any(), anyString(), anyString())).thenReturn("http://example.com/cert.jpg");
        when(ocrService.parseRepairCert(anyString())).thenReturn(certInfo);
        when(repairmanMapper.selectByUserId(anyString())).thenReturn(null);
        when(repairmanMapper.insert(any(Repairman.class))).thenReturn(1);
        when(roleMapper.selectByRoleCode("ROLE_REPAIRMAN")).thenReturn(repairmanRole);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        boolean result = qualificationService.uploadRepairmanCert(certVerifyDTO);

        assertTrue(result);
        verify(repairmanMapper).insert(any(Repairman.class));
    }

    @Test
    @DisplayName("上传维修资格证 - 未完成实名认证")
    void uploadRepairmanCert_NotIdCardVerified() {
        testUser.setRealName(null);
        certVerifyDTO.setDrivingLicenseBase64(null);
        certVerifyDTO.setRepairCertBase64("data:image/png;base64,repair");

        when(userMapper.selectById(1L)).thenReturn(testUser);

        assertThrows(UserException.class, () -> qualificationService.uploadRepairmanCert(certVerifyDTO));
    }

    @Test
    @DisplayName("身份证验证 - OCR返回空信息")
    void verifyIdCard_EmptyOcrInfo() throws Exception {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(storageService.upload(any(), anyString(), anyString())).thenReturn("http://example.com/idcard.jpg");
        when(ocrService.parseIdCard(anyString())).thenReturn(new HashMap<>());

        assertThrows(UserException.class, () -> qualificationService.verifyIdCard(idCardVerifyDTO));
    }

    @Test
    @DisplayName("身份证验证 - 图片为空")
    void verifyIdCard_NullImage() {
        idCardVerifyDTO.setIdCardFrontBase64(null);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        assertThrows(UserException.class, () -> qualificationService.verifyIdCard(idCardVerifyDTO));
    }

    @Test
    @DisplayName("上传驾驶证 - OCR返回空信息")
    void uploadDriverCert_EmptyOcrInfo() throws Exception {
        testUser.setRealName("张三");
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(storageService.upload(any(), anyString(), anyString())).thenReturn("http://example.com/license.jpg");
        when(ocrService.parseDrivingLicense(anyString())).thenReturn(new HashMap<>());

        assertThrows(UserException.class, () -> qualificationService.uploadDriverCert(certVerifyDTO));
    }

    @Test
    @DisplayName("上传驾驶证 - 图片为空")
    void uploadDriverCert_NullImage() {
        testUser.setRealName("张三");
        certVerifyDTO.setDrivingLicenseBase64(null);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        assertThrows(UserException.class, () -> qualificationService.uploadDriverCert(certVerifyDTO));
    }
}
