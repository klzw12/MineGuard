package com.klzw.service.user.controller;

import com.klzw.common.core.enums.UserStatusEnum;
import com.klzw.common.file.enums.FileBusinessTypeEnum;
import com.klzw.common.file.impl.FileUploadServiceImpl;
import com.klzw.common.web.resolver.CurrentUser;
import com.klzw.service.user.dto.AdminCreateUserDTO;
import com.klzw.service.user.dto.PasswordUpdateDTO;
import com.klzw.service.user.dto.UserUpdateDTO;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 切片测试
 */
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private FileUploadServiceImpl fileUploadService;

    @InjectMocks
    private UserController userController;

    private UserVO testUserVO;
    private Role testRole;
    private Long currentUserId = 1L;

    // 自定义参数解析器，用于模拟 @CurrentUser 注解
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
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(currentUserResolver)
                .build();

        testUserVO = new UserVO();
        testUserVO.setId("1");
        testUserVO.setUsername("testuser");
        testUserVO.setRealName("测试用户");
        testUserVO.setPhone("13800138000");
        testUserVO.setEmail("test@example.com");
        testUserVO.setStatus(UserStatusEnum.ENABLED.getValue());

        testRole = new Role();
        testRole.setId(1L);
        testRole.setRoleCode("ROLE_USER");
        testRole.setRoleName("普通用户");
    }

    @Test
    @DisplayName("获取当前用户信息 - 成功")
    void getCurrentUser_Success() throws Exception {
        when(userService.getCurrentUser(1L)).thenReturn(testUserVO);

        mockMvc.perform(get("/user/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService).getCurrentUser(1L);
    }

    @Test
    @DisplayName("获取当前用户信息 - 用户不存在")
    void getCurrentUser_NotFound() throws Exception {
        when(userService.getCurrentUser(1L)).thenReturn(null);

        mockMvc.perform(get("/user/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).getCurrentUser(1L);
    }

    @Test
    @DisplayName("更新用户信息 - 成功")
    void updateUser_Success() throws Exception {
        UserVO updatedVO = new UserVO();
        updatedVO.setId("1");
        updatedVO.setRealName("更新后的姓名");

        when(userService.updateUserInfo(eq(1L), any(UserUpdateDTO.class))).thenReturn(updatedVO);

        mockMvc.perform(put("/user/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"realName\":\"更新后的姓名\",\"phone\":\"13800138001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).updateUserInfo(eq(1L), any(UserUpdateDTO.class));
    }

    @Test
    @DisplayName("修改密码 - 成功")
    void updatePassword_Success() throws Exception {
        doNothing().when(userService).updatePassword(eq(1L), any(PasswordUpdateDTO.class));

        mockMvc.perform(put("/user/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"oldPassword\":\"old123\",\"newPassword\":\"new456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).updatePassword(eq(1L), any(PasswordUpdateDTO.class));
    }

    @Test
    @DisplayName("获取当前用户角色 - 成功")
    void getCurrentUserRole_Success() throws Exception {
        when(userService.getRoleByUserId(1L)).thenReturn(testRole);

        mockMvc.perform(get("/user/role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).getRoleByUserId(1L);
    }

    @Test
    @DisplayName("获取当前用户角色 - 用户无角色")
    void getCurrentUserRole_NoRole() throws Exception {
        when(userService.getRoleByUserId(1L)).thenReturn(null);

        mockMvc.perform(get("/user/role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).getRoleByUserId(1L);
    }

    @Test
    @DisplayName("获取当前用户角色编码 - 成功")
    void getCurrentUserRoleCode_Success() throws Exception {
        when(userService.getRoleCodeByUserId(1L)).thenReturn("ROLE_USER");

        mockMvc.perform(get("/user/role-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).getRoleCodeByUserId(1L);
    }

    @Test
    @DisplayName("获取当前用户角色编码 - 用户无角色")
    void getCurrentUserRoleCode_NoRole() throws Exception {
        when(userService.getRoleCodeByUserId(1L)).thenReturn(null);

        mockMvc.perform(get("/user/role-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).getRoleCodeByUserId(1L);
    }

    @Test
    @DisplayName("上传用户头像 - 成功")
    void uploadAvatar_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        when(fileUploadService.upload(any(), eq(FileBusinessTypeEnum.USER_AVATAR), eq("1")))
                .thenReturn("avatar/1/avatar.jpg");
        when(userService.updateAvatar(eq(1L), anyString())).thenReturn(testUserVO);

        mockMvc.perform(multipart("/user/avatar")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(fileUploadService).upload(any(), eq(FileBusinessTypeEnum.USER_AVATAR), eq("1"));
        verify(userService).updateAvatar(eq(1L), anyString());
    }

    @Test
    @DisplayName("上传用户头像 - 上传失败")
    void uploadAvatar_UploadFailed() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        when(fileUploadService.upload(any(), eq(FileBusinessTypeEnum.USER_AVATAR), anyString()))
                .thenThrow(new RuntimeException("上传失败"));

        mockMvc.perform(multipart("/user/avatar")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));

        verify(userService, never()).updateAvatar(anyLong(), anyString());
    }

    @Test
    @DisplayName("管理员创建用户 - 成功")
    void adminCreateUser_Success() throws Exception {
        when(userService.adminCreateUser(any(AdminCreateUserDTO.class))).thenReturn("2");

        mockMvc.perform(post("/user/admin/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newuser\",\"password\":\"Password123\",\"realName\":\"新用户\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("2"));

        verify(userService).adminCreateUser(any(AdminCreateUserDTO.class));
    }
}
