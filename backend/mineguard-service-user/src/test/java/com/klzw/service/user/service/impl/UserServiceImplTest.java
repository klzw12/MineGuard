package com.klzw.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.auth.util.PasswordUtils;
import com.klzw.common.core.enums.UserStatusEnum;
import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.service.user.dto.AdminCreateUserDTO;
import com.klzw.service.user.dto.PasswordUpdateDTO;
import com.klzw.service.user.dto.UserRegisterDTO;
import com.klzw.service.user.dto.UserUpdateDTO;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.exception.UserException;
import com.klzw.service.user.mapper.RoleMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.vo.UserVO;
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
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private PasswordUtils passwordUtils;

    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserVO testUserVO;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRealName("测试用户");
        testUser.setPhone("13800138000");
        testUser.setEmail("test@example.com");
        testUser.setStatus(UserStatusEnum.ENABLED.getValue());
        testUser.setRoleId(1L);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());

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
    @DisplayName("根据用户名获取用户 - 成功")
    void getByUsername_Success() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        User result = userService.getByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据用户名获取用户 - 用户不存在")
    void getByUsername_NotFound() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        User result = userService.getByUsername("nonexistent");

        assertNull(result);
        verify(userMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据ID获取用户 - 成功")
    void getById_Success() {
        when(userMapper.selectById(1L)).thenReturn(testUser);

        User result = userService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userMapper).selectById(1L);
    }

    @Test
    @DisplayName("获取用户VO - 从缓存获取")
    void getUserVOById_FromCache() {
        when(redisCacheService.get("user:info:1")).thenReturn(testUserVO);

        UserVO result = userService.getUserVOById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(redisCacheService).get("user:info:1");
        verify(userMapper, never()).selectById(anyLong());
    }

    @Test
    @DisplayName("获取用户VO - 从数据库获取并缓存")
    void getUserVOById_FromDatabase() {
        when(redisCacheService.get("user:info:1")).thenReturn(null);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(roleMapper.selectById(1L)).thenReturn(testRole);

        UserVO result = userService.getUserVOById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(redisCacheService).set(eq("user:info:1"), any(UserVO.class), eq(30L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("获取用户VO - 用户不存在")
    void getUserVOById_NotFound() {
        when(redisCacheService.get("user:info:1")).thenReturn(null);
        when(userMapper.selectById(1L)).thenReturn(null);

        UserVO result = userService.getUserVOById(1L);

        assertNull(result);
    }

    @Test
    @DisplayName("注册用户 - 成功")
    void register_Success() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("Test123");
        dto.setPhone("13800138001");
        dto.setSmsCode("1234");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(passwordUtils.encode("Test123")).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return 1;
        });
        when(userMapper.selectById(2L)).thenReturn(testUser);
        when(roleMapper.selectById(anyLong())).thenReturn(testRole);

        UserVO result = userService.register(dto);

        assertNotNull(result);
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("注册用户 - 用户名已存在")
    void register_UsernameExists() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("testuser");
        dto.setPassword("Test123");
        dto.setPhone("13800138001");
        dto.setSmsCode("1234");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        assertThrows(UserException.class, () -> userService.register(dto));
    }

    @Test
    @DisplayName("更新用户信息 - 成功")
    void updateUserInfo_Success() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setUsername("updateduser");
        dto.setEmail("updated@test.com");

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(roleMapper.selectById(anyLong())).thenReturn(testRole);

        UserVO result = userService.updateUserInfo(1L, dto);

        assertNotNull(result);
        verify(redisCacheService).delete("user:info:1");
    }

    @Test
    @DisplayName("更新用户信息 - 用户不存在")
    void updateUserInfo_UserNotFound() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setUsername("updateduser");

        when(userMapper.selectById(999L)).thenReturn(null);

        assertThrows(UserException.class, () -> userService.updateUserInfo(999L, dto));
    }

    @Test
    @DisplayName("更新密码 - 成功")
    void updatePassword_Success() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("oldPassword");
        dto.setNewPassword("NewTest123");

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(passwordUtils.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordUtils.encode("NewTest123")).thenReturn("newEncodedPassword");
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> userService.updatePassword(1L, dto));
        verify(redisCacheService).delete("user:info:1");
    }

    @Test
    @DisplayName("更新密码 - 旧密码错误")
    void updatePassword_OldPasswordError() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("wrongPassword");
        dto.setNewPassword("NewTest123");

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(passwordUtils.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(UserException.class, () -> userService.updatePassword(1L, dto));
    }

    @Test
    @DisplayName("禁用用户 - 成功")
    void disableUser_Success() {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> userService.disableUser(1L));
        verify(redisCacheService).delete("user:info:1");
    }

    @Test
    @DisplayName("启用用户 - 成功")
    void enableUser_Success() {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> userService.enableUser(1L));
        verify(redisCacheService).delete("user:info:1");
    }

    @Test
    @DisplayName("分配角色 - 成功")
    void assignRole_Success() {
        Role role = new Role();
        role.setId(2L);
        role.setRoleCode("ROLE_DRIVER");
        role.setRoleName("司机");

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(roleMapper.selectById(2L)).thenReturn(role);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> userService.assignRole(1L, 2L));
        verify(redisCacheService).delete("user:info:1");
    }

    @Test
    @DisplayName("获取角色编码 - 成功")
    void getRoleCodeByUserId_Success() {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(roleMapper.selectById(1L)).thenReturn(testRole);

        String result = userService.getRoleCodeByUserId(1L);

        assertEquals("ROLE_USER", result);
    }

    @Test
    @DisplayName("获取角色编码 - 用户无角色")
    void getRoleCodeByUserId_NoRole() {
        testUser.setRoleId(null);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        String result = userService.getRoleCodeByUserId(1L);

        assertNull(result);
    }

    @Test
    @DisplayName("获取角色 - 成功")
    void getRoleByUserId_Success() {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(roleMapper.selectById(1L)).thenReturn(testRole);

        Role result = userService.getRoleByUserId(1L);

        assertNotNull(result);
        assertEquals("ROLE_USER", result.getRoleCode());
    }

    @Test
    @DisplayName("更新头像 - 成功")
    void updateAvatar_Success() {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(roleMapper.selectById(anyLong())).thenReturn(testRole);

        UserVO result = userService.updateAvatar(1L, "avatar/new.jpg");

        assertNotNull(result);
        assertEquals("avatar/new.jpg", testUser.getAvatarUrl());
        verify(redisCacheService).delete("user:info:1");
    }

    @Test
    @DisplayName("管理员创建用户 - 成功")
    void adminCreateUser_Success() {
        AdminCreateUserDTO dto = new AdminCreateUserDTO();
        dto.setUsername("admincreated");
        dto.setPassword("Admin123");
        dto.setRealName("管理员创建的用户");
        dto.setPhone("13800138003");
        dto.setEmail("admin@example.com");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(passwordUtils.encode("Admin123")).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(3L);
            return 1;
        });

        String result = userService.adminCreateUser(dto);

        assertNotNull(result);
        assertEquals("3", result);
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("管理员创建用户 - 用户名已存在")
    void adminCreateUser_UsernameExists() {
        AdminCreateUserDTO dto = new AdminCreateUserDTO();
        dto.setUsername("testuser");
        dto.setPassword("Admin123");
        dto.setRealName("管理员创建的用户");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        assertThrows(UserException.class, () -> userService.adminCreateUser(dto));
    }

    @Test
    @DisplayName("根据手机号获取用户 - 成功")
    void getByPhone_Success() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        User result = userService.getByPhone("13800138000");

        assertNotNull(result);
        assertEquals("13800138000", result.getPhone());
    }

    @Test
    @DisplayName("创建用户 - 成功")
    void createUser_Success() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password");

        when(userMapper.insert(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> userService.createUser(newUser));
        verify(userMapper).insert(newUser);
    }

    @Test
    @DisplayName("分页查询用户 - 成功")
    void pageUsers_Success() {
        Page<User> userPage = new Page<>();
        userPage.setRecords(Arrays.asList(testUser));
        userPage.setTotal(1);

        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(userPage);
        when(roleMapper.selectById(anyLong())).thenReturn(testRole);

        Page<UserVO> result = userService.pageUsers(1, 10, "test", null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("清除用户缓存 - 成功")
    void clearUserCache_Success() {
        assertDoesNotThrow(() -> userService.clearUserCache(1L));
        verify(redisCacheService).delete("user:info:1");
    }
}
