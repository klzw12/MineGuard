package com.klzw.service.user.service.impl;

import com.klzw.service.user.entity.RoleChangeApply;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.mapper.RoleChangeApplyMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.vo.RoleChangeApplyVO;
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
 * 角色变更申请服务单元测试
 * 
 * 测试范围：
 * - 创建角色变更申请
 * - 查询申请列表
 * - 处理角色变更申请（通过/拒绝）
 * - 边界条件和异常场景
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleChangeApplyServiceImplTest {

    @Mock
    private RoleChangeApplyMapper roleChangeApplyMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private RoleChangeApplyServiceImpl roleChangeApplyService;

    private RoleChangeApply testApply;
    private User testUser;
    private User testUserWithRole;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");
        testUser.setRoleId(null);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());

        // 创建已有角色的用户
        testUserWithRole = new User();
        testUserWithRole.setId(1L);
        testUserWithRole.setUsername("testuser");
        testUserWithRole.setRealName("测试用户");
        testUserWithRole.setRoleId(1L);
        testUserWithRole.setCreateTime(LocalDateTime.now());
        testUserWithRole.setUpdateTime(LocalDateTime.now());

        // 创建测试申请
        testApply = new RoleChangeApply();
        testApply.setId(1L);
        testApply.setUserId(1L);
        testApply.setUsername("testuser");
        testApply.setCurrentRoleId(1L);
        testApply.setCurrentRoleCode("ROLE_USER");
        testApply.setCurrentRoleName("普通用户");
        testApply.setApplyRoleId(2L);
        testApply.setApplyRoleCode("ROLE_DRIVER");
        testApply.setApplyRoleName("司机");
        testApply.setApplyReason("希望成为司机");
        testApply.setStatus(1); // 待处理
        testApply.setCreateTime(LocalDateTime.now());
        testApply.setUpdateTime(LocalDateTime.now());
    }

    // ==================== 创建申请测试 ====================

    @Test
    @DisplayName("创建角色变更申请 - 成功")
    void createRoleChangeApply_Success() {
        RoleChangeApply apply = new RoleChangeApply();
        apply.setUserId(1L);
        apply.setUsername("testuser");
        apply.setApplyRoleId(2L);
        apply.setApplyRoleCode("ROLE_DRIVER");
        apply.setApplyRoleName("司机");
        apply.setApplyReason("希望成为司机");

        when(roleChangeApplyMapper.insert(any(RoleChangeApply.class))).thenAnswer(invocation -> {
            RoleChangeApply savedApply = invocation.getArgument(0);
            savedApply.setId(1L);
            return 1;
        });

        String result = roleChangeApplyService.createRoleChangeApply(apply);

        assertNotNull(result);
        assertEquals("1", result);
        assertEquals(1, apply.getStatus()); // 验证默认状态为待处理
        assertNotNull(apply.getCreateTime());
        assertNotNull(apply.getUpdateTime());
        verify(roleChangeApplyMapper).insert(any(RoleChangeApply.class));
    }

    @Test
    @DisplayName("创建角色变更申请 - 验证默认值设置")
    void createRoleChangeApply_DefaultValuesSet() {
        RoleChangeApply apply = new RoleChangeApply();
        apply.setUserId(1L);
        apply.setApplyRoleId(2L);

        when(roleChangeApplyMapper.insert(any(RoleChangeApply.class))).thenReturn(1);

        roleChangeApplyService.createRoleChangeApply(apply);

        assertEquals(1, apply.getStatus()); // 默认待处理状态
        assertNotNull(apply.getCreateTime());
        assertNotNull(apply.getUpdateTime());
    }

    // ==================== 查询申请测试 ====================

    @Test
    @DisplayName("根据ID获取申请 - 成功")
    void getRoleChangeApplyById_Success() {
        when(roleChangeApplyMapper.selectById(1L)).thenReturn(testApply);

        RoleChangeApply result = roleChangeApplyService.getRoleChangeApplyById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("ROLE_DRIVER", result.getApplyRoleCode());
        verify(roleChangeApplyMapper).selectById(1L);
    }

    @Test
    @DisplayName("根据ID获取申请 - 不存在")
    void getRoleChangeApplyById_NotFound() {
        when(roleChangeApplyMapper.selectById(999L)).thenReturn(null);

        RoleChangeApply result = roleChangeApplyService.getRoleChangeApplyById(999L);

        assertNull(result);
        verify(roleChangeApplyMapper).selectById(999L);
    }

    @Test
    @DisplayName("根据用户ID获取申请列表 - 成功")
    void getRoleChangeAppliesByUserId_Success() {
        RoleChangeApply apply2 = new RoleChangeApply();
        apply2.setId(2L);
        apply2.setUserId(1L);
        apply2.setUsername("testuser");
        apply2.setApplyRoleId(3L);
        apply2.setApplyRoleCode("ROLE_SAFETY_OFFICER");
        apply2.setApplyRoleName("安全员");
        apply2.setStatus(2); // 已通过
        apply2.setCreateTime(LocalDateTime.now());
        apply2.setUpdateTime(LocalDateTime.now());

        when(roleChangeApplyMapper.selectByUserId(1L)).thenReturn(Arrays.asList(testApply, apply2));

        List<RoleChangeApplyVO> result = roleChangeApplyService.getRoleChangeAppliesByUserId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals("ROLE_DRIVER", result.get(0).getApplyRoleCode());
        assertEquals("ROLE_SAFETY_OFFICER", result.get(1).getApplyRoleCode());
        verify(roleChangeApplyMapper).selectByUserId(1L);
    }

    @Test
    @DisplayName("根据用户ID获取申请列表 - 空列表")
    void getRoleChangeAppliesByUserId_EmptyList() {
        when(roleChangeApplyMapper.selectByUserId(999L)).thenReturn(Collections.emptyList());

        List<RoleChangeApplyVO> result = roleChangeApplyService.getRoleChangeAppliesByUserId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(roleChangeApplyMapper).selectByUserId(999L);
    }

    @Test
    @DisplayName("获取待处理申请列表 - 成功")
    void getPendingRoleChangeApplies_Success() {
        when(roleChangeApplyMapper.selectPendingApplies()).thenReturn(Arrays.asList(testApply));

        List<RoleChangeApplyVO> result = roleChangeApplyService.getPendingRoleChangeApplies();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getStatus()); // 验证都是待处理状态
        verify(roleChangeApplyMapper).selectPendingApplies();
    }

    @Test
    @DisplayName("获取所有申请列表 - 成功")
    void getAllRoleChangeApplies_Success() {
        RoleChangeApply approvedApply = new RoleChangeApply();
        approvedApply.setId(2L);
        approvedApply.setUserId(2L);
        approvedApply.setUsername("user2");
        approvedApply.setStatus(2); // 已通过
        approvedApply.setCreateTime(LocalDateTime.now());
        approvedApply.setUpdateTime(LocalDateTime.now());

        when(roleChangeApplyMapper.selectList(null)).thenReturn(Arrays.asList(testApply, approvedApply));

        List<RoleChangeApplyVO> result = roleChangeApplyService.getAllRoleChangeApplies();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(roleChangeApplyMapper).selectList(null);
    }

    // ==================== 处理申请测试 ====================

    @Test
    @DisplayName("处理申请 - 通过并更新用户角色")
    void handleRoleChangeApply_Approve_Success() {
        when(roleChangeApplyMapper.selectById(1L)).thenReturn(testApply);
        when(roleChangeApplyMapper.updateById(any(RoleChangeApply.class))).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(testUserWithRole);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        boolean result = roleChangeApplyService.handleRoleChangeApply(1L, 2, "同意申请", 10L, "管理员");

        assertTrue(result);
        assertEquals(2, testApply.getStatus()); // 已通过
        assertEquals("同意申请", testApply.getAdminOpinion());
        assertNotNull(testApply.getHandleTime());
        assertEquals(10L, testApply.getHandlerId());
        assertEquals("管理员", testApply.getHandlerName());
        verify(userMapper).updateById(any(User.class));
        verify(roleChangeApplyMapper).updateById(any(RoleChangeApply.class));
    }

    @Test
    @DisplayName("处理申请 - 拒绝申请")
    void handleRoleChangeApply_Reject_Success() {
        when(roleChangeApplyMapper.selectById(1L)).thenReturn(testApply);
        when(roleChangeApplyMapper.updateById(any(RoleChangeApply.class))).thenReturn(1);

        boolean result = roleChangeApplyService.handleRoleChangeApply(1L, 3, "不符合条件", 10L, "管理员");

        assertTrue(result);
        assertEquals(3, testApply.getStatus()); // 已拒绝
        assertEquals("不符合条件", testApply.getAdminOpinion());
        assertNotNull(testApply.getHandleTime());
        // 拒绝时不应该更新用户角色
        verify(userMapper, never()).updateById(any(User.class));
        verify(roleChangeApplyMapper).updateById(any(RoleChangeApply.class));
    }

    @Test
    @DisplayName("处理申请 - 申请不存在")
    void handleRoleChangeApply_NotFound() {
        when(roleChangeApplyMapper.selectById(999L)).thenReturn(null);

        boolean result = roleChangeApplyService.handleRoleChangeApply(999L, 2, "同意", 10L, "管理员");

        assertFalse(result);
        verify(roleChangeApplyMapper, never()).updateById(any(RoleChangeApply.class));
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("处理申请 - 申请已处理")
    void handleRoleChangeApply_AlreadyHandled() {
        testApply.setStatus(2); // 已通过

        when(roleChangeApplyMapper.selectById(1L)).thenReturn(testApply);

        boolean result = roleChangeApplyService.handleRoleChangeApply(1L, 3, "拒绝", 10L, "管理员");

        assertFalse(result);
        // 不应该再次更新
        verify(roleChangeApplyMapper, never()).updateById(any(RoleChangeApply.class));
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("处理申请 - 通过时用户不存在")
    void handleRoleChangeApply_Approve_UserNotFound() {
        when(roleChangeApplyMapper.selectById(1L)).thenReturn(testApply);
        when(roleChangeApplyMapper.updateById(any(RoleChangeApply.class))).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(null);

        boolean result = roleChangeApplyService.handleRoleChangeApply(1L, 2, "同意申请", 10L, "管理员");

        assertTrue(result);
        // 申请状态仍然更新
        assertEquals(2, testApply.getStatus());
        // 用户不存在时不应该更新用户
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("处理申请 - 通过时更新用户角色ID")
    void handleRoleChangeApply_Approve_UpdateUserRole() {
        testUserWithRole.setRoleId(1L); // 当前角色

        when(roleChangeApplyMapper.selectById(1L)).thenReturn(testApply);
        when(roleChangeApplyMapper.updateById(any(RoleChangeApply.class))).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(testUserWithRole);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        boolean result = roleChangeApplyService.handleRoleChangeApply(1L, 2, "同意", 10L, "管理员");

        assertTrue(result);
        assertEquals(2L, testUserWithRole.getRoleId()); // 角色ID应该更新为申请的角色ID
        assertNotNull(testUserWithRole.getUpdateTime());
        verify(userMapper).updateById(testUserWithRole);
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("处理申请 - 空管理员意见")
    void handleRoleChangeApply_EmptyOpinion() {
        when(roleChangeApplyMapper.selectById(1L)).thenReturn(testApply);
        when(roleChangeApplyMapper.updateById(any(RoleChangeApply.class))).thenReturn(1);

        boolean result = roleChangeApplyService.handleRoleChangeApply(1L, 2, null, 10L, "管理员");

        assertTrue(result);
        assertNull(testApply.getAdminOpinion());
    }

    @Test
    @DisplayName("处理申请 - 不同状态值")
    void handleRoleChangeApply_DifferentStatus() {
        // 测试状态2（已通过）
        when(roleChangeApplyMapper.selectById(1L)).thenReturn(testApply);
        when(roleChangeApplyMapper.updateById(any(RoleChangeApply.class))).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(testUserWithRole);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        boolean result = roleChangeApplyService.handleRoleChangeApply(1L, 2, "通过", 10L, "管理员");
        assertTrue(result);
        assertEquals(2, testApply.getStatus());

        // 重置状态测试状态3（已拒绝）
        testApply.setStatus(1);
        result = roleChangeApplyService.handleRoleChangeApply(1L, 3, "拒绝", 10L, "管理员");
        assertTrue(result);
        assertEquals(3, testApply.getStatus());
    }

    @Test
    @DisplayName("创建申请 - 完整信息")
    void createRoleChangeApply_FullInfo() {
        RoleChangeApply apply = new RoleChangeApply();
        apply.setUserId(1L);
        apply.setUsername("testuser");
        apply.setCurrentRoleId(1L);
        apply.setCurrentRoleCode("ROLE_USER");
        apply.setCurrentRoleName("普通用户");
        apply.setApplyRoleId(2L);
        apply.setApplyRoleCode("ROLE_DRIVER");
        apply.setApplyRoleName("司机");
        apply.setApplyReason("我有驾驶执照，希望成为司机");

        when(roleChangeApplyMapper.insert(any(RoleChangeApply.class))).thenAnswer(invocation -> {
            RoleChangeApply savedApply = invocation.getArgument(0);
            savedApply.setId(1L);
            return 1;
        });

        String result = roleChangeApplyService.createRoleChangeApply(apply);

        assertEquals("1", result);
        verify(roleChangeApplyMapper).insert(apply);
    }
}
