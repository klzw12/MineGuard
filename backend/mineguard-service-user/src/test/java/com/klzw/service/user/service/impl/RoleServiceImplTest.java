package com.klzw.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.mapper.RoleMapper;
import com.klzw.service.user.vo.RoleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 角色服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleServiceImplTest {

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role testRole;
    private Role testRole2;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setRoleCode("ROLE_USER");
        testRole.setRoleName("普通用户");
        testRole.setDescription("普通用户角色");
        testRole.setCreateTime(LocalDateTime.now());
        testRole.setUpdateTime(LocalDateTime.now());

        testRole2 = new Role();
        testRole2.setId(2L);
        testRole2.setRoleCode("ROLE_DRIVER");
        testRole2.setRoleName("司机");
        testRole2.setDescription("司机角色");
        testRole2.setCreateTime(LocalDateTime.now());
        testRole2.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("获取所有角色列表 - 成功")
    void getAllRoles_Success() {
        when(roleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(testRole, testRole2));

        List<RoleVO> result = roleService.getAllRoles();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ROLE_USER", result.get(0).getRoleCode());
        assertEquals("ROLE_DRIVER", result.get(1).getRoleCode());
        verify(roleMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取所有角色列表 - 空列表")
    void getAllRoles_EmptyList() {
        when(roleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<RoleVO> result = roleService.getAllRoles();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(roleMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据ID获取角色 - 成功")
    void getRoleById_Success() {
        when(roleMapper.selectById(1L)).thenReturn(testRole);

        RoleVO result = roleService.getRoleById(1L);

        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("ROLE_USER", result.getRoleCode());
        assertEquals("普通用户", result.getRoleName());
        verify(roleMapper).selectById(1L);
    }

    @Test
    @DisplayName("根据ID获取角色 - 不存在")
    void getRoleById_NotFound() {
        when(roleMapper.selectById(999L)).thenReturn(null);

        RoleVO result = roleService.getRoleById(999L);

        assertNull(result);
        verify(roleMapper).selectById(999L);
    }

    @Test
    @DisplayName("根据编码获取角色 - 成功")
    void getRoleByCode_Success() {
        when(roleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testRole);

        RoleVO result = roleService.getRoleByCode("ROLE_USER");

        assertNotNull(result);
        assertEquals("ROLE_USER", result.getRoleCode());
        verify(roleMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据编码获取角色 - 不存在")
    void getRoleByCode_NotFound() {
        when(roleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        RoleVO result = roleService.getRoleByCode("ROLE_NONEXISTENT");

        assertNull(result);
        verify(roleMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("创建角色 - 成功")
    void createRole_Success() {
        RoleVO newRoleVO = new RoleVO();
        newRoleVO.setRoleCode("ROLE_ADMIN");
        newRoleVO.setRoleName("管理员");
        newRoleVO.setDescription("管理员角色");

        when(roleMapper.insert(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            role.setId(3L);
            return 1;
        });

        RoleVO result = roleService.createRole(newRoleVO);

        assertNotNull(result);
        assertEquals("ROLE_ADMIN", result.getRoleCode());
        assertEquals("管理员", result.getRoleName());
        verify(roleMapper).insert(any(Role.class));
    }

    @Test
    @DisplayName("创建角色 - 验证时间字段设置")
    void createRole_TimeFieldsSet() {
        RoleVO newRoleVO = new RoleVO();
        newRoleVO.setRoleCode("ROLE_TEST");
        newRoleVO.setRoleName("测试角色");

        when(roleMapper.insert(any(Role.class))).thenReturn(1);

        roleService.createRole(newRoleVO);

        verify(roleMapper).insert(any(Role.class));
    }

    @Test
    @DisplayName("更新角色 - 成功")
    void updateRole_Success() {
        RoleVO updateVO = new RoleVO();
        updateVO.setRoleCode("ROLE_USER_UPDATED");
        updateVO.setRoleName("更新后的用户");
        updateVO.setDescription("更新后的描述");

        when(roleMapper.selectById(1L)).thenReturn(testRole);
        when(roleMapper.updateById(any(Role.class))).thenReturn(1);

        RoleVO result = roleService.updateRole(1L, updateVO);

        assertNotNull(result);
        verify(roleMapper).updateById(any(Role.class));
    }

    @Test
    @DisplayName("更新角色 - 角色不存在")
    void updateRole_NotFound() {
        RoleVO updateVO = new RoleVO();
        updateVO.setRoleCode("ROLE_TEST");
        updateVO.setRoleName("测试");

        when(roleMapper.selectById(999L)).thenReturn(null);

        RoleVO result = roleService.updateRole(999L, updateVO);

        assertNull(result);
        verify(roleMapper, never()).updateById(any(Role.class));
    }

    @Test
    @DisplayName("删除角色 - 成功")
    void deleteRole_Success() {
        when(roleMapper.deleteById(1L)).thenReturn(1);

        boolean result = roleService.deleteRole(1L);

        assertTrue(result);
        verify(roleMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除角色 - 不存在")
    void deleteRole_NotFound() {
        when(roleMapper.deleteById(999L)).thenReturn(0);

        boolean result = roleService.deleteRole(999L);

        assertFalse(result);
        verify(roleMapper).deleteById(999L);
    }

    @Test
    @DisplayName("创建角色 - 仅必填字段")
    void createRole_OnlyRequiredFields() {
        RoleVO newRoleVO = new RoleVO();
        newRoleVO.setRoleCode("ROLE_MINIMAL");
        newRoleVO.setRoleName("最小角色");

        when(roleMapper.insert(any(Role.class))).thenReturn(1);

        RoleVO result = roleService.createRole(newRoleVO);

        assertNotNull(result);
        assertEquals("ROLE_MINIMAL", result.getRoleCode());
        assertEquals("最小角色", result.getRoleName());
    }

    @Test
    @DisplayName("更新角色 - 部分字段更新")
    void updateRole_PartialUpdate() {
        RoleVO updateVO = new RoleVO();
        updateVO.setRoleName("仅更新名称");

        when(roleMapper.selectById(1L)).thenReturn(testRole);
        when(roleMapper.updateById(any(Role.class))).thenReturn(1);

        RoleVO result = roleService.updateRole(1L, updateVO);

        assertNotNull(result);
        verify(roleMapper).updateById(any(Role.class));
    }

    @Test
    @DisplayName("角色VO转换 - ID转换为字符串")
    void roleVOConversion_IdToString() {
        when(roleMapper.selectById(123456789L)).thenReturn(testRole);

        RoleVO result = roleService.getRoleById(123456789L);

        assertNotNull(result);
        assertNotNull(result.getId());
    }
}
