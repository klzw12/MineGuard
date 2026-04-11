package com.klzw.service.user;

import com.klzw.common.core.config.DotenvInitializer;
import com.klzw.service.user.dto.UserRegisterDTO;
import com.klzw.service.user.dto.UserUpdateDTO;
import com.klzw.service.user.dto.PasswordUpdateDTO;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.UserVO;
import com.klzw.common.core.enums.UserStatusEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MineguardUserServiceApplication.class)
@ContextConfiguration(initializers = DotenvInitializer.class)
@ActiveProfiles("test")
@Tag("integration")
@Transactional
public class UserIntegrationTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;

    private UserRegisterDTO registerDTO;
    private UserVO registeredUser;

    @BeforeAll
    static void beforeAll(@Autowired UserMapper userMapper) {
        // 使用truncate table清空user表，避免受deleted字段影响
        userMapper.truncateTable();
    }

    @BeforeEach
    void setUp() {
        // 使用动态生成的用户名和手机号，避免用户名重复导致的注册失败
        String uniqueUsername = "testuser" + System.currentTimeMillis();
        String uniquePhone = "138" + System.currentTimeMillis() % 100000000;
        registerDTO = new UserRegisterDTO();
        registerDTO.setUsername(uniqueUsername);
        registerDTO.setPassword("password123");
        registerDTO.setPhone(uniquePhone);
        registerDTO.setEmail(uniqueUsername + "@example.com");
        registeredUser = userService.register(registerDTO);
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        if (registerDTO != null) {
            // 通过用户名查询用户，获取实际的用户id，然后删除用户
            User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>().eq(User::getUsername, registerDTO.getUsername()));
            if (user != null) {
                userMapper.deleteById(user.getId());
            }
        }
    }

    @Test
    void testRegisterAndGetUser() {
        assertNotNull(registeredUser);
        assertEquals(registerDTO.getUsername(), registeredUser.getUsername());
        assertEquals(registerDTO.getPhone(), registeredUser.getPhone());
        assertEquals(registerDTO.getEmail(), registeredUser.getEmail());
        assertEquals(UserStatusEnum.DISABLED.getValue(), registeredUser.getStatus());

        // 通过用户名查询用户，获取实际的用户id
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>().eq(User::getUsername, registerDTO.getUsername()));
        assertNotNull(user);
        Long actualUserId = user.getId();

        // 获取用户信息
        UserVO retrievedUser = userService.getUserVOById(actualUserId);
        assertNotNull(retrievedUser);
        assertEquals(actualUserId.toString(), retrievedUser.getId());
        assertEquals(registerDTO.getUsername(), retrievedUser.getUsername());
    }

    @Test
    void testUpdateUserInfo() {
        // 通过用户名查询用户，获取实际的用户id
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>().eq(User::getUsername, registerDTO.getUsername()));
        assertNotNull(user);
        Long actualUserId = user.getId();

        // 更新用户信息
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("updateduser");
        updateDTO.setEmail("updated@example.com");

        UserVO updatedUser = userService.updateUserInfo(actualUserId, updateDTO);
        assertNotNull(updatedUser);
        assertEquals("updateduser", updatedUser.getUsername());
        assertEquals("updated@example.com", updatedUser.getEmail());

        // 验证更新后的数据
        UserVO retrievedUser = userService.getUserVOById(actualUserId);
        assertNotNull(retrievedUser);
        assertEquals("updateduser", retrievedUser.getUsername());
        assertEquals("updated@example.com", retrievedUser.getEmail());
    }

    @Test
    void testUpdatePassword() {
        // 通过用户名查询用户，获取实际的用户id
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>().eq(User::getUsername, registerDTO.getUsername()));
        assertNotNull(user);
        Long actualUserId = user.getId();

        // 更新密码
        PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO();
        passwordUpdateDTO.setOldPassword("password123");
        passwordUpdateDTO.setNewPassword("newpassword123");

        userService.updatePassword(actualUserId, passwordUpdateDTO);

        // 验证密码更新成功（这里只能验证方法执行成功，无法直接验证密码是否正确更新，因为密码是加密存储的）
        UserVO retrievedUser = userService.getUserVOById(actualUserId);
        assertNotNull(retrievedUser);
        assertEquals(actualUserId.toString(), retrievedUser.getId());
    }

    @Test
    void testDisableAndEnableUser() {
        // 通过用户名查询用户，获取实际的用户id
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>().eq(User::getUsername, registerDTO.getUsername()));
        assertNotNull(user);
        Long actualUserId = user.getId();

        // 禁用用户
        userService.disableUser(actualUserId);
        UserVO disabledUser = userService.getUserVOById(actualUserId);
        assertEquals(UserStatusEnum.DISABLED.getValue(), disabledUser.getStatus());

        // 启用用户
        userService.enableUser(actualUserId);
        UserVO enabledUser = userService.getUserVOById(actualUserId);
        assertEquals(UserStatusEnum.ENABLED.getValue(), enabledUser.getStatus());
    }

    @Test
    void testUserExists() {
        // 通过用户名查询用户，获取实际的用户id
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>().eq(User::getUsername, registerDTO.getUsername()));
        assertNotNull(user);
        Long actualUserId = user.getId();

        // 验证用户存在
        boolean exists = userService.existsUser(actualUserId);
        assertTrue(exists);

        // 验证不存在的用户
        boolean notExists = userService.existsUser(999L);
        assertFalse(notExists);
    }
}
