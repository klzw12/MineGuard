package com.klzw.service.user.controller;

import com.klzw.service.user.dto.CertVerifyDTO;
import com.klzw.service.user.dto.IdCardVerifyDTO;
import com.klzw.service.user.service.QualificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class QualificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private QualificationService qualificationService;

    @InjectMocks
    private QualificationController qualificationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(qualificationController).build();
    }

    @Test
    @DisplayName("身份证验证 - 成功")
    void verifyIdCard_Success() throws Exception {
        when(qualificationService.verifyIdCard(any(IdCardVerifyDTO.class))).thenReturn(true);

        mockMvc.perform(post("/api/qualification/idcard/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"realName\":\"张三\",\"idCard\":\"110101199001011234\",\"idCardFrontBase64\":\"data:image/png;base64,test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("身份证验证成功"))
                .andExpect(jsonPath("$.data").value(true));

        verify(qualificationService).verifyIdCard(any(IdCardVerifyDTO.class));
    }

    @Test
    @DisplayName("检查身份证验证状态 - 已验证")
    void checkIdCardVerified_True() throws Exception {
        when(qualificationService.checkIdCardVerified(1L)).thenReturn(true);

        mockMvc.perform(get("/api/qualification/idcard/check/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(qualificationService).checkIdCardVerified(1L);
    }

    @Test
    @DisplayName("检查身份证验证状态 - 未验证")
    void checkIdCardVerified_False() throws Exception {
        when(qualificationService.checkIdCardVerified(1L)).thenReturn(false);

        mockMvc.perform(get("/api/qualification/idcard/check/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));

        verify(qualificationService).checkIdCardVerified(1L);
    }

    @Test
    @DisplayName("上传驾驶证 - 成功")
    void uploadDriverCert_Success() throws Exception {
        when(qualificationService.uploadDriverCert(any(CertVerifyDTO.class))).thenReturn(true);

        mockMvc.perform(post("/api/qualification/cert/driver")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"certNumber\":\"JZ123456789\",\"drivingLicenseBase64\":\"data:image/png;base64,test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("驾驶证上传成功"))
                .andExpect(jsonPath("$.data").value(true));

        verify(qualificationService).uploadDriverCert(any(CertVerifyDTO.class));
    }

    @Test
    @DisplayName("上传应急救援证 - 成功")
    void uploadSafetyOfficerCert_Success() throws Exception {
        when(qualificationService.uploadSafetyOfficerCert(any(CertVerifyDTO.class))).thenReturn(true);

        mockMvc.perform(post("/api/qualification/cert/safety-officer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"certNumber\":\"AQ123456789\",\"emergencyCertBase64\":\"data:image/png;base64,test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("应急救援证上传成功"))
                .andExpect(jsonPath("$.data").value(true));

        verify(qualificationService).uploadSafetyOfficerCert(any(CertVerifyDTO.class));
    }

    @Test
    @DisplayName("上传维修资格证 - 成功")
    void uploadRepairmanCert_Success() throws Exception {
        when(qualificationService.uploadRepairmanCert(any(CertVerifyDTO.class))).thenReturn(true);

        mockMvc.perform(post("/api/qualification/cert/repairman")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"certNumber\":\"WX123456789\",\"repairCertBase64\":\"data:image/png;base64,test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("维修资格证上传成功"))
                .andExpect(jsonPath("$.data").value(true));

        verify(qualificationService).uploadRepairmanCert(any(CertVerifyDTO.class));
    }
}
