package com.klzw.service.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.enums.UserStatusEnum;
import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.common.file.impl.FileUploadServiceImpl;
import com.klzw.service.user.dto.PasswordUpdateDTO;
import com.klzw.service.user.dto.UserRegisterDTO;
import com.klzw.service.user.dto.UserUpdateDTO;
import com.klzw.service.user.dto.AdminCreateUserDTO;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.exception.UserException;
import com.klzw.service.user.constant.UserResultCode;
import com.klzw.common.core.exception.BaseException;
import com.klzw.common.core.enums.ResultCodeEnum;
import com.klzw.service.user.mapper.RoleMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.mapper.UserAttendanceMapper;
import com.klzw.service.user.service.impl.UserServiceImpl;
import com.klzw.service.user.vo.UserVO;
import com.klzw.service.user.vo.IdCardVO;
import com.klzw.service.user.service.sms.SmsService;

import com.klzw.common.auth.util.PasswordUtils;
import com.klzw.common.auth.enums.RoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private UserAttendanceMapper userAttendanceMapper;

    @Mock
    private PasswordUtils passwordUtils;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private FileUploadServiceImpl fileUploadService;

    @Mock
    private SmsService smsService;

    @Mock
    private MultipartFile multipartFile;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setPhone("13800138000");
        user.setEmail("test@example.com");
        user.setStatus(UserStatusEnum.ENABLED.getValue());
        user.setRoleId(1L);
        user.setDeleted(0);
        user.setDeleted(0);
        user.setDeleted(0);

        role = new Role();
        role.setId(1L);
        role.setRoleCode(RoleEnum.ADMIN.getValue());
        role.setRoleName("管理员");
    }

    @Test
    void getByUsername() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        User result = userService.getByUsername("testuser");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getById() {
        when(userMapper.selectById(1L)).thenReturn(user);
        User result = userService.getById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getUserVOById() {
        when(redisCacheService.get("user:info:1")).thenReturn(null);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(roleMapper.selectById(1L)).thenReturn(role);
        UserVO result = userService.getUserVOById(1L);
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals(RoleEnum.ADMIN.getValue(), result.getRoleCode());
        verify(redisCacheService, times(1)).set(eq("user:info:1"), any(UserVO.class), eq(30L), eq(TimeUnit.MINUTES));
    }

    @Test
    void getCurrentUser() {
        UserVO userVO = new UserVO();
        userVO.setId("1");
        userVO.setUsername("testuser");
        when(redisCacheService.get("user:info:1")).thenReturn(userVO);
        UserVO result = userService.getCurrentUser(1L);
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void register() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("password");
        dto.setPhone("13900139000");
        dto.setEmail("newuser@example.com");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(passwordUtils.encode("password")).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(2L);
            return 1;
        });

        UserVO result = userService.register(dto);
        assertNotNull(result);
        assertEquals("2", result.getId());
        assertEquals("newuser", result.getUsername());
    }

    @Test
    void register_UsernameExists() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("testuser");
        dto.setPassword("password");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        UserException exception = assertThrows(UserException.class, () -> userService.register(dto));
        assertEquals(UserResultCode.USERNAME_EXISTS.getCode(), exception.getCode());
    }

    @Test
    void updateUserInfo() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setUsername("updateduser");
        dto.setEmail("updated@example.com");

        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        UserVO result = userService.updateUserInfo(1L, dto);
        assertNotNull(result);
        assertEquals("1", result.getId());
        verify(redisCacheService, times(1)).delete("user:info:1");
    }

    @Test
    void updatePassword() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("password");
        dto.setNewPassword("newpassword");

        when(userMapper.selectById(1L)).thenReturn(user);
        when(passwordUtils.matches("password", user.getPassword())).thenReturn(true);
        when(passwordUtils.encode("newpassword")).thenReturn("encodedNewPassword");
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.updatePassword(1L, dto);
        verify(redisCacheService, times(1)).delete("user:info:1");
    }

    @Test
    void updatePassword_OldPasswordError() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("wrongpassword");
        dto.setNewPassword("newpassword");

        when(userMapper.selectById(1L)).thenReturn(user);
        when(passwordUtils.matches("wrongpassword", user.getPassword())).thenReturn(false);

        UserException exception = assertThrows(UserException.class, () -> userService.updatePassword(1L, dto));
        assertEquals(UserResultCode.OLD_PASSWORD_ERROR.getCode(), exception.getCode());
    }

    @Test
    void pageUsers() {
        List<User> users = new ArrayList<>();
        users.add(user);

        Page<User> userPage = new Page<>(1, 10);
        userPage.setRecords(users);
        userPage.setTotal(1L);

        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(userPage);
        when(roleMapper.selectById(1L)).thenReturn(role);

        Page<UserVO> result = userService.pageUsers(1, 10, "test", UserStatusEnum.ENABLED.getValue());
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void disableUser() {
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.disableUser(1L);
        verify(userMapper, times(1)).updateById(any(User.class));
        verify(redisCacheService, times(1)).delete("user:info:1");
    }

    @Test
    void enableUser() {
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.enableUser(1L);
        verify(userMapper, times(1)).updateById(any(User.class));
        verify(redisCacheService, times(1)).delete("user:info:1");
    }

    @Test
    void assignRole() {
        when(userMapper.selectById(1L)).thenReturn(user);
        when(roleMapper.selectById(1L)).thenReturn(role);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.assignRole(1L, 1L);
        verify(userMapper, times(1)).updateById(any(User.class));
        verify(redisCacheService, times(1)).delete("user:info:1");
    }

    @Test
    void adminAssignRole() {
        when(userMapper.selectById(1L)).thenReturn(user);
        when(roleMapper.selectById(1L)).thenReturn(role);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.adminAssignRole(1L, 1L);
        verify(userMapper, times(1)).updateById(any(User.class));
        verify(redisCacheService, times(1)).delete("user:info:1");
    }

    @Test
    void adminAssignRole_InvalidRole() {
        Role invalidRole = new Role();
        invalidRole.setId(2L);
        invalidRole.setRoleCode("INVALID_ROLE");

        when(userMapper.selectById(1L)).thenReturn(user);
        when(roleMapper.selectById(2L)).thenReturn(invalidRole);

        BaseException exception = assertThrows(BaseException.class, () -> userService.adminAssignRole(1L, 2L));
        assertEquals(ResultCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void getRoleCodeByUserId() {
        when(userMapper.selectById(1L)).thenReturn(user);
        when(roleMapper.selectById(1L)).thenReturn(role);

        String roleCode = userService.getRoleCodeByUserId(1L);
        assertEquals(RoleEnum.ADMIN.getValue(), roleCode);
    }

    @Test
    void getRoleByUserId() {
        when(userMapper.selectById(1L)).thenReturn(user);
        when(roleMapper.selectById(1L)).thenReturn(role);

        Role result = userService.getRoleByUserId(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(RoleEnum.ADMIN.getValue(), result.getRoleCode());
    }

    @Test
    void clearUserCache() {
        userService.clearUserCache(1L);
        verify(redisCacheService, times(1)).delete("user:info:1");
    }

    @Test
    void getByPhone() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        User result = userService.getByPhone("13800138000");
        assertNotNull(result);
        assertEquals("13800138000", result.getPhone());
    }

    @Test
    void createUser() {
        when(userMapper.insert(any(User.class))).thenReturn(1);
        userService.createUser(user);
        verify(userMapper, times(1)).insert(user);
    }

    @Test
    void uploadAvatar() throws IOException {
        when(userMapper.selectById(1L)).thenReturn(user);
        when(fileUploadService.upload(any(MultipartFile.class), any(), any())).thenReturn("avatar/path");
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(redisCacheService.get("user:info:1")).thenReturn(null);
        when(roleMapper.selectById(1L)).thenReturn(role);

        UserVO result = userService.uploadAvatar(1L, multipartFile);
        assertNotNull(result);
        verify(redisCacheService, times(1)).delete("user:info:1");
    }

    @Test
    void adminCreateUser() {
        AdminCreateUserDTO dto = new AdminCreateUserDTO();
        dto.setUsername("adminuser");
        dto.setPassword("password");
        dto.setPhone("13700137000");
        dto.setEmail("admin@example.com");
        dto.setRoleId(1L);

        // 创建一个新的User对象用于测试
        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("adminuser");
        savedUser.setPassword("encodedPassword");
        savedUser.setPhone("13700137000");
        savedUser.setEmail("admin@example.com");
        savedUser.setRoleId(1L);
        savedUser.setStatus(UserStatusEnum.DISABLED.getValue());
        savedUser.setDeleted(0);

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(roleMapper.selectById(1L)).thenReturn(role);
        when(passwordUtils.encode("password")).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return 1;
        });
        when(userMapper.selectById(2L)).thenReturn(savedUser);
        when(redisCacheService.get("user:info:2")).thenReturn(null);
        doNothing().when(redisCacheService).set(anyString(), any(UserVO.class), anyLong(), any(TimeUnit.class));

        UserVO result = userService.adminCreateUser(dto);
        assertNotNull(result);
        assertEquals("2", result.getId());
        assertEquals("adminuser", result.getUsername());
    }

    @Test
    void updatePhone() {
        when(userMapper.selectById(1L)).thenReturn(user);
        when(smsService.verifySmsCode("13800138001", "123456")).thenReturn(true);
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(redisCacheService.get("user:info:1")).thenReturn(null);
        when(roleMapper.selectById(1L)).thenReturn(role);

        UserVO result = userService.updatePhone(1L, "13800138001", "123456");
        assertNotNull(result);
        verify(redisCacheService, times(1)).delete("user:info:1");
    }

    @Test
    void updatePhone_SmsVerifyFailed() {
        when(userMapper.selectById(1L)).thenReturn(user);
        when(smsService.verifySmsCode("13800138001", "123456")).thenReturn(false);

        UserException exception = assertThrows(UserException.class, () -> userService.updatePhone(1L, "13800138001", "123456"));
        assertEquals(UserResultCode.SMS_VERIFY_FAILED.getCode(), exception.getCode());
    }

    @Test
    void getUsersByRoleCode() {
        List<User> users = new ArrayList<>();
        users.add(user);

        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(users);
        when(roleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(role);
        when(roleMapper.selectById(1L)).thenReturn(role);

        List<UserVO> result = userService.getUsersByRoleCode(RoleEnum.ADMIN.getValue());
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAvatarSignedUrl() {
        user.setAvatarUrl("avatar/path");
        when(redisCacheService.get("avatar:signed_url:1")).thenReturn(null);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(fileUploadService.getSignedUrl("avatar/path", 7 * 24 * 3600L)).thenReturn("signed-url");

        String result = userService.getAvatarSignedUrl(1L);
        assertEquals("signed-url", result);
        verify(redisCacheService, times(1)).set(eq("avatar:signed_url:1"), eq("signed-url"), eq(7 * 24 * 3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    void getIdCardSignedUrls() {
        user.setRealName("张三");
        user.setIdCardFrontUrl("idcard/front");
        user.setIdCardBackUrl("idcard/back");

        when(redisCacheService.get("idcard:signed_url:1")).thenReturn(null);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(fileUploadService.getSignedUrl("idcard/front", 3600L)).thenReturn("front-signed-url");
        when(fileUploadService.getSignedUrl("idcard/back", 3600L)).thenReturn("back-signed-url");

        IdCardVO result = userService.getIdCardSignedUrls(1L);
        assertNotNull(result);
        assertEquals("张三", result.getRealName());
        assertEquals("front-signed-url", result.getFrontUrl());
        assertEquals("back-signed-url", result.getBackUrl());
    }

    @Test
    void searchContacts() {
        List<User> users = new ArrayList<>();
        users.add(user);

        Page<User> userPage = new Page<>(1, 10);
        userPage.setRecords(users);
        userPage.setTotal(1L);

        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(userPage);
        when(roleMapper.selectById(1L)).thenReturn(role);

        Page<UserVO> result = userService.searchContacts("test", RoleEnum.ADMIN.getValue(), 1, 10);
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void existsUser() {
        when(userMapper.selectById(1L)).thenReturn(user);
        boolean result = userService.existsUser(1L);
        assertTrue(result);
    }

    @Test
    void existsUser_NotFound() {
        when(userMapper.selectById(1L)).thenReturn(null);
        boolean result = userService.existsUser(1L);
        assertFalse(result);
    }
}
