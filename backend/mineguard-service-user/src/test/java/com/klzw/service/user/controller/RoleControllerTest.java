package com.klzw.service.user.controller;

import com.klzw.service.user.service.RoleService;
import com.klzw.service.user.vo.RoleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RoleController 切片测试
 */
class RoleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    private RoleVO testRoleVO;
    private RoleVO testRoleVO2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();

        testRoleVO = new RoleVO();
        testRoleVO.setId("1");
        testRoleVO.setRoleCode("ROLE_USER");
        testRoleVO.setRoleName("普通用户");
        testRoleVO.setDescription("普通用户角色");

        testRoleVO2 = new RoleVO();
        testRoleVO2.setId("2");
        testRoleVO2.setRoleCode("ROLE_DRIVER");
        testRoleVO2.setRoleName("司机");
        testRoleVO2.setDescription("司机角色");
    }

    @Test
    @DisplayName("获取角色列表 - 成功")
    void getAllRoles_Success() throws Exception {
        when(roleService.getAllRoles()).thenReturn(Arrays.asList(testRoleVO, testRoleVO2));

        mockMvc.perform(get("/api/role/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(roleService).getAllRoles();
    }

    @Test
    @DisplayName("获取角色列表 - 空列表")
    void getAllRoles_EmptyList() throws Exception {
        when(roleService.getAllRoles()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/role/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(roleService).getAllRoles();
    }

    @Test
    @DisplayName("获取角色详情 - 成功")
    void getRoleById_Success() throws Exception {
        when(roleService.getRoleById(1L)).thenReturn(testRoleVO);

        mockMvc.perform(get("/api/role/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.roleCode").value("ROLE_USER"));

        verify(roleService).getRoleById(1L);
    }

    @Test
    @DisplayName("获取角色详情 - 不存在")
    void getRoleById_NotFound() throws Exception {
        when(roleService.getRoleById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/role/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(roleService).getRoleById(999L);
    }

    @Test
    @DisplayName("创建角色 - 成功")
    void createRole_Success() throws Exception {
        RoleVO createdRole = new RoleVO();
        createdRole.setId("3");
        createdRole.setRoleCode("ROLE_ADMIN");
        createdRole.setRoleName("管理员");

        when(roleService.createRole(any(RoleVO.class))).thenReturn(createdRole);

        mockMvc.perform(post("/api/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"ROLE_ADMIN\",\"roleName\":\"管理员\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.roleCode").value("ROLE_ADMIN"));

        verify(roleService).createRole(any(RoleVO.class));
    }

    @Test
    @DisplayName("更新角色 - 成功")
    void updateRole_Success() throws Exception {
        RoleVO updatedRole = new RoleVO();
        updatedRole.setId("1");
        updatedRole.setRoleCode("ROLE_USER_UPDATED");
        updatedRole.setRoleName("更新后的用户");

        when(roleService.updateRole(eq(1L), any(RoleVO.class))).thenReturn(updatedRole);

        mockMvc.perform(put("/api/role/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"ROLE_USER_UPDATED\",\"roleName\":\"更新后的用户\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(roleService).updateRole(eq(1L), any(RoleVO.class));
    }

    @Test
    @DisplayName("删除角色 - 成功")
    void deleteRole_Success() throws Exception {
        when(roleService.deleteRole(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/role/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(roleService).deleteRole(1L);
    }

    @Test
    @DisplayName("删除角色 - 不存在")
    void deleteRole_NotFound() throws Exception {
        when(roleService.deleteRole(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/role/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));

        verify(roleService).deleteRole(999L);
    }
}
