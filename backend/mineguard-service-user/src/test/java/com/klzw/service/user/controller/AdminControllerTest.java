package com.klzw.service.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    void testPageUsers() throws Exception {
        int pageNum = 1;
        int pageSize = 10;
        String username = "test";
        Integer status = 1;

        Page<UserVO> expectedPage = new Page<>();
        expectedPage.setTotal(1);
        expectedPage.setCurrent(pageNum);
        expectedPage.setSize(pageSize);

        when(userService.pageUsers(pageNum, pageSize, username, status)).thenReturn(expectedPage);

        mockMvc.perform(get("/user/admin/page")
                .param("pageNum", String.valueOf(pageNum))
                .param("pageSize", String.valueOf(pageSize))
                .param("username", username)
                .param("status", String.valueOf(status)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).pageUsers(pageNum, pageSize, username, status);
    }

    @Test
    void testGetUserById() throws Exception {
        Long userId = 123L;

        UserVO expectedVO = new UserVO();
        expectedVO.setId(String.valueOf(userId));
        expectedVO.setUsername("testuser");

        when(userService.getUserVOById(userId)).thenReturn(expectedVO);

        mockMvc.perform(get("/user/admin/user/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).getUserVOById(userId);
    }

    @Test
    void testDisableUser() throws Exception {
        Long userId = 123L;

        doNothing().when(userService).disableUser(userId);

        mockMvc.perform(put("/user/admin/user/{id}/disable", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).disableUser(userId);
    }

    @Test
    void testEnableUser() throws Exception {
        Long userId = 123L;

        doNothing().when(userService).enableUser(userId);

        mockMvc.perform(put("/user/admin/user/{id}/enable", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).enableUser(userId);
    }

    @Test
    void testGetUserRole() throws Exception {
        Long userId = 123L;

        Role expectedRole = new Role();
        expectedRole.setId(123L);
        expectedRole.setRoleCode("DRIVER");
        expectedRole.setRoleName("司机");

        when(userService.getRoleByUserId(userId)).thenReturn(expectedRole);

        mockMvc.perform(get("/user/admin/user/{id}/role", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).getRoleByUserId(userId);
    }

    @Test
    void testGetUserRoleCode() throws Exception {
        Long userId = 123L;
        String expectedRoleCode = "DRIVER";

        when(userService.getRoleCodeByUserId(userId)).thenReturn(expectedRoleCode);

        mockMvc.perform(get("/user/admin/user/{id}/role-code", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).getRoleCodeByUserId(userId);
    }

    @Test
    void testChangeUserRole_UserNotFound() throws Exception {
        Long userId = 123L;
        Long roleId = 456L;
        String reason = "角色变更";

        when(userService.getUserVOById(userId)).thenReturn(null);

        // 由于 Controller 抛出异常，没有全局异常处理器时会抛出异常
        // 这里我们验证方法被调用，异常处理暂时不测试
        try {
            mockMvc.perform(put("/user/admin/user/{id}/role-change", userId)
                    .param("roleId", String.valueOf(roleId))
                    .param("reason", reason));
        } catch (Exception e) {
            // 预期会抛出异常，因为 Controller 抛出了 UserException
        }

        verify(userService, times(1)).getUserVOById(userId);
        verify(userService, never()).getRoleByUserId(anyLong());
        verify(userService, never()).assignRole(anyLong(), anyLong());
    }

    @Test
    void testChangeUserRole_NoExistingRole() throws Exception {
        Long userId = 123L;
        Long roleId = 456L;
        String reason = "角色变更";

        UserVO userVO = new UserVO();
        userVO.setId(String.valueOf(userId));

        when(userService.getUserVOById(userId)).thenReturn(userVO);
        when(userService.getRoleByUserId(userId)).thenReturn(null);
        doNothing().when(userService).assignRole(userId, roleId);

        mockMvc.perform(put("/user/admin/user/{id}/role-change", userId)
                .param("roleId", String.valueOf(roleId))
                .param("reason", reason))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).getUserVOById(userId);
        verify(userService, times(1)).getRoleByUserId(userId);
        verify(userService, times(1)).assignRole(userId, roleId);
    }

    @Test
    void testChangeUserRole_WithExistingRole() throws Exception {
        Long userId = 123L;
        Long roleId = 456L;
        String reason = "角色变更";

        UserVO userVO = new UserVO();
        userVO.setId(String.valueOf(userId));

        Role currentRole = new Role();
        currentRole.setId(789L);
        currentRole.setRoleCode("OLD_ROLE");

        when(userService.getUserVOById(userId)).thenReturn(userVO);
        when(userService.getRoleByUserId(userId)).thenReturn(currentRole);
        doNothing().when(userService).assignRole(userId, roleId);

        mockMvc.perform(put("/user/admin/user/{id}/role-change", userId)
                .param("roleId", String.valueOf(roleId))
                .param("reason", reason))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).getUserVOById(userId);
        verify(userService, times(1)).getRoleByUserId(userId);
        verify(userService, times(1)).assignRole(userId, roleId);
    }
}
