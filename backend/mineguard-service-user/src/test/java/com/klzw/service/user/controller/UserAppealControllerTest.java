package com.klzw.service.user.controller;

import com.klzw.common.web.resolver.CurrentUser;
import com.klzw.service.user.dto.HandleAppealDTO;
import com.klzw.service.user.dto.UserAppealDTO;
import com.klzw.service.user.service.UserAppealService;
import com.klzw.service.user.vo.UserAppealVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserAppealController 切片测试
 */
class UserAppealControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserAppealService userAppealService;

    @InjectMocks
    private UserAppealController userAppealController;

    private UserAppealVO testAppealVO;
    private Long currentUserId = 1L;

    private final HandlerMethodArgumentResolver currentUserResolver = new HandlerMethodArgumentResolver() {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(CurrentUser.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return currentUserId;
        }
    };

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        mockMvc = MockMvcBuilders.standaloneSetup(userAppealController)
                .setCustomArgumentResolvers(currentUserResolver)
                .build();

        testAppealVO = new UserAppealVO();
        testAppealVO.setId("1");
        testAppealVO.setUserId("1");
        testAppealVO.setUsername("testuser");
        testAppealVO.setRealName("测试用户");
        testAppealVO.setPhone("13800138000");
        testAppealVO.setAppealReason("账号被误禁用");
        testAppealVO.setStatus(1);
    }

    @Test
    @DisplayName("提交申诉 - 成功")
    void createAppeal_Success() throws Exception {
        when(userAppealService.createAppeal(eq(1L), any(UserAppealDTO.class))).thenReturn("1");

        mockMvc.perform(post("/api/user/appeal")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appealReason\":\"账号被误禁用，请求解除\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("申诉提交成功"))
                .andExpect(jsonPath("$.data").value("1"));

        verify(userAppealService).createAppeal(eq(1L), any(UserAppealDTO.class));
    }

    @Test
    @DisplayName("获取当前用户申诉列表 - 成功")
    void getMyAppeals_Success() throws Exception {
        when(userAppealService.getAppealsByUserId(1L)).thenReturn(Arrays.asList(testAppealVO));

        mockMvc.perform(get("/api/user/appeal/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(userAppealService).getAppealsByUserId(1L);
    }

    @Test
    @DisplayName("获取当前用户申诉列表 - 空列表")
    void getMyAppeals_EmptyList() throws Exception {
        when(userAppealService.getAppealsByUserId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/user/appeal/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(userAppealService).getAppealsByUserId(1L);
    }

    @Test
    @DisplayName("检查是否有待处理申诉 - 有")
    void hasPendingAppeal_True() throws Exception {
        when(userAppealService.hasPendingAppeal(1L)).thenReturn(true);

        mockMvc.perform(get("/api/user/appeal/pending/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(userAppealService).hasPendingAppeal(1L);
    }

    @Test
    @DisplayName("检查是否有待处理申诉 - 无")
    void hasPendingAppeal_False() throws Exception {
        when(userAppealService.hasPendingAppeal(1L)).thenReturn(false);

        mockMvc.perform(get("/api/user/appeal/pending/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));

        verify(userAppealService).hasPendingAppeal(1L);
    }

    @Test
    @DisplayName("管理员获取待处理申诉列表 - 成功")
    void getPendingAppeals_Success() throws Exception {
        when(userAppealService.getPendingAppeals()).thenReturn(Arrays.asList(testAppealVO));

        mockMvc.perform(get("/api/user/appeal/admin/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userAppealService).getPendingAppeals();
    }

    @Test
    @DisplayName("管理员获取所有申诉列表 - 成功")
    void getAllAppeals_Success() throws Exception {
        when(userAppealService.getAllAppeals()).thenReturn(Arrays.asList(testAppealVO));

        mockMvc.perform(get("/api/user/appeal/admin/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userAppealService).getAllAppeals();
    }

    @Test
    @DisplayName("管理员处理申诉 - 成功")
    void handleAppeal_Success() throws Exception {
        when(userAppealService.handleAppeal(eq(1L), any(HandleAppealDTO.class))).thenReturn(true);

        mockMvc.perform(put("/api/user/appeal/admin/1/handle")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":2,\"adminOpinion\":\"同意解除\",\"handlerId\":10,\"handlerName\":\"管理员\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("申诉处理成功"));

        verify(userAppealService).handleAppeal(eq(1L), any(HandleAppealDTO.class));
    }

    @Test
    @DisplayName("管理员处理申诉 - 失败")
    void handleAppeal_Failed() throws Exception {
        when(userAppealService.handleAppeal(eq(1L), any(HandleAppealDTO.class))).thenReturn(false);

        mockMvc.perform(put("/api/user/appeal/admin/1/handle")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":2,\"handlerId\":10,\"handlerName\":\"管理员\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));

        verify(userAppealService).handleAppeal(eq(1L), any(HandleAppealDTO.class));
    }

    @Test
    @DisplayName("管理员根据用户ID获取申诉列表 - 成功")
    void getAppealsByUserId_Success() throws Exception {
        when(userAppealService.getAppealsByUserId(1L)).thenReturn(Arrays.asList(testAppealVO));

        mockMvc.perform(get("/api/user/appeal/admin/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userAppealService).getAppealsByUserId(1L);
    }
}
