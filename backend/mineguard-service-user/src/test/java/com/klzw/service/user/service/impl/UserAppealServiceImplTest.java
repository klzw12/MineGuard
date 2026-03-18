package com.klzw.service.user.service.impl;

import com.klzw.common.core.enums.UserStatusEnum;
import com.klzw.service.user.dto.HandleAppealDTO;
import com.klzw.service.user.dto.UserAppealDTO;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.entity.UserAppeal;
import com.klzw.service.user.mapper.UserAppealMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.UserAppealVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAppealServiceImplTest {

    @Mock
    private UserAppealMapper userAppealMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserAppealServiceImpl userAppealService;

    private User testUser;
    private UserAppeal testAppeal;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");
        testUser.setPhone("13800138000");
        testUser.setStatus(UserStatusEnum.DISABLED.getValue());
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());

        testAppeal = new UserAppeal();
        testAppeal.setId(1L);
        testAppeal.setUserId(1L);
        testAppeal.setUsername("testuser");
        testAppeal.setRealName("测试用户");
        testAppeal.setPhone("13800138000");
        testAppeal.setAppealReason("账号被误禁用");
        testAppeal.setStatus(1); // 待处理
        testAppeal.setCreateTime(LocalDateTime.now());
        testAppeal.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("创建申诉 - 成功")
    void createAppeal_Success() {
        UserAppealDTO dto = new UserAppealDTO();
        dto.setAppealReason("账号被误禁用，请求解除");

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userAppealMapper.countPendingByUserId(1L)).thenReturn(0);
        when(userAppealMapper.insert(any(UserAppeal.class))).thenAnswer(invocation -> {
            UserAppeal appeal = invocation.getArgument(0);
            appeal.setId(1L);
            return 1;
        });

        String result = userAppealService.createAppeal(1L, dto);

        assertNotNull(result);
        assertEquals("1", result);
        verify(userAppealMapper).insert(any(UserAppeal.class));
    }

    @Test
    @DisplayName("创建申诉 - 用户不存在")
    void createAppeal_UserNotFound() {
        UserAppealDTO dto = new UserAppealDTO();
        dto.setAppealReason("账号被误禁用");

        when(userMapper.selectById(999L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userAppealService.createAppeal(999L, dto));
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("创建申诉 - 用户未被禁用")
    void createAppeal_UserNotDisabled() {
        UserAppealDTO dto = new UserAppealDTO();
        dto.setAppealReason("账号被误禁用");

        testUser.setStatus(UserStatusEnum.ENABLED.getValue());
        when(userMapper.selectById(1L)).thenReturn(testUser);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userAppealService.createAppeal(1L, dto));
        assertEquals("用户账号未被禁用，无需申诉", exception.getMessage());
    }

    @Test
    @DisplayName("创建申诉 - 已有待处理申诉")
    void createAppeal_HasPendingAppeal() {
        UserAppealDTO dto = new UserAppealDTO();
        dto.setAppealReason("账号被误禁用");

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userAppealMapper.countPendingByUserId(1L)).thenReturn(1);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userAppealService.createAppeal(1L, dto));
        assertEquals("您已有一个待处理的申诉，请勿重复提交", exception.getMessage());
    }

    @Test
    @DisplayName("根据ID获取申诉 - 成功")
    void getAppealById_Success() {
        when(userAppealMapper.selectById(1L)).thenReturn(testAppeal);

        UserAppealVO result = userAppealService.getAppealById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("账号被误禁用", result.getAppealReason());
    }

    @Test
    @DisplayName("根据用户ID获取申诉列表 - 成功")
    void getAppealsByUserId_Success() {
        when(userAppealMapper.selectByUserId(1L)).thenReturn(Arrays.asList(testAppeal));

        List<UserAppealVO> result = userAppealService.getAppealsByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    @DisplayName("获取待处理申诉列表 - 成功")
    void getPendingAppeals_Success() {
        when(userAppealMapper.selectPendingApplies()).thenReturn(Arrays.asList(testAppeal));

        List<UserAppealVO> result = userAppealService.getPendingAppeals();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getStatus());
    }

    @Test
    @DisplayName("获取所有申诉列表 - 成功")
    void getAllAppeals_Success() {
        UserAppeal appeal2 = new UserAppeal();
        appeal2.setId(2L);
        appeal2.setUserId(2L);
        appeal2.setUsername("user2");
        appeal2.setStatus(2); // 已通过
        appeal2.setCreateTime(LocalDateTime.now());
        appeal2.setUpdateTime(LocalDateTime.now());

        when(userAppealMapper.selectList(null)).thenReturn(Arrays.asList(testAppeal, appeal2));

        List<UserAppealVO> result = userAppealService.getAllAppeals();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("处理申诉 - 通过")
    void handleAppeal_Approve() {
        HandleAppealDTO dto = new HandleAppealDTO();
        dto.setStatus(2); // 已通过
        dto.setAdminOpinion("同意解除禁用");
        dto.setHandlerId(1L);
        dto.setHandlerName("管理员");

        when(userAppealMapper.selectById(1L)).thenReturn(testAppeal);
        when(userAppealMapper.updateById(any(UserAppeal.class))).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        doNothing().when(userService).clearUserCache(1L);

        boolean result = userAppealService.handleAppeal(1L, dto);

        assertTrue(result);
        verify(userMapper).updateById(any(User.class));
        verify(userService).clearUserCache(1L);
    }

    @Test
    @DisplayName("处理申诉 - 驳回")
    void handleAppeal_Reject() {
        HandleAppealDTO dto = new HandleAppealDTO();
        dto.setStatus(3); // 已驳回
        dto.setAdminOpinion("申诉理由不充分");
        dto.setHandlerId(1L);
        dto.setHandlerName("管理员");

        when(userAppealMapper.selectById(1L)).thenReturn(testAppeal);
        when(userAppealMapper.updateById(any(UserAppeal.class))).thenReturn(1);

        boolean result = userAppealService.handleAppeal(1L, dto);

        assertTrue(result);
        verify(userAppealMapper).updateById(any(UserAppeal.class));
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("处理申诉 - 驳回并删除账号")
    void handleAppeal_RejectAndDelete() {
        HandleAppealDTO dto = new HandleAppealDTO();
        dto.setStatus(4); // 已驳回并删除账号
        dto.setAdminOpinion("恶意申诉，删除账号");
        dto.setHandlerId(1L);
        dto.setHandlerName("管理员");

        when(userAppealMapper.selectById(1L)).thenReturn(testAppeal);
        when(userAppealMapper.updateById(any(UserAppeal.class))).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        doNothing().when(userService).clearUserCache(1L);

        boolean result = userAppealService.handleAppeal(1L, dto);

        assertTrue(result);
        verify(userMapper).updateById(any(User.class));
        verify(userService).clearUserCache(1L);
    }

    @Test
    @DisplayName("处理申诉 - 申诉不存在")
    void handleAppeal_NotFound() {
        HandleAppealDTO dto = new HandleAppealDTO();
        dto.setStatus(2);
        dto.setHandlerId(1L);
        dto.setHandlerName("管理员");

        when(userAppealMapper.selectById(999L)).thenReturn(null);

        boolean result = userAppealService.handleAppeal(999L, dto);

        assertFalse(result);
    }

    @Test
    @DisplayName("处理申诉 - 申诉已处理")
    void handleAppeal_AlreadyHandled() {
        testAppeal.setStatus(2); // 已处理

        HandleAppealDTO dto = new HandleAppealDTO();
        dto.setStatus(2);
        dto.setHandlerId(1L);
        dto.setHandlerName("管理员");

        when(userAppealMapper.selectById(1L)).thenReturn(testAppeal);

        boolean result = userAppealService.handleAppeal(1L, dto);

        assertFalse(result);
    }

    @Test
    @DisplayName("检查是否有待处理申诉 - 有")
    void hasPendingAppeal_True() {
        when(userAppealMapper.countPendingByUserId(1L)).thenReturn(1);

        boolean result = userAppealService.hasPendingAppeal(1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("检查是否有待处理申诉 - 无")
    void hasPendingAppeal_False() {
        when(userAppealMapper.countPendingByUserId(1L)).thenReturn(0);

        boolean result = userAppealService.hasPendingAppeal(1L);

        assertFalse(result);
    }
}
