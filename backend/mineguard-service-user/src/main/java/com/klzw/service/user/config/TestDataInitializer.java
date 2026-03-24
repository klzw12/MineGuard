package com.klzw.service.user.config;

import com.klzw.common.auth.util.PasswordUtils;
import com.klzw.common.core.enums.UserStatusEnum;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 测试数据初始化器
 * 在 dev/test 环境下自动创建测试用户（安全员、调度员、维修员）
 * 通过 mineguard.init-user.enable 控制是否启用
 * 只绕过验证码，其他业务流程（实名认证等）正常进行
 */
@Slf4j
@Component
@Profile({"dev", "test"})
@RequiredArgsConstructor
public class TestDataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordUtils passwordUtils;

    @Value("${mineguard.init-user.enable:false}")
    private boolean initUserEnable;

    // 测试手机号列表（3 个用户，身份通过后续资格认证分配）
    private static final List<TestUserData> TEST_USERS = List.of(
        new TestUserData("13800138001", "safe", "12Qw.."),
        new TestUserData("138000138002", "dispatch", "12Qw.."),
        new TestUserData("138000138003", "repair", "12Qw..")
    );

    @Override
    @Transactional
    public void run(String... args) {
        // 检查是否启用初始化
        if (!initUserEnable) {
            log.debug("测试数据初始化未启用，设置 mineguard.init-user.enable=true 以启用");
            return;
        }
        
        log.info("开始初始化测试数据...");
        
        try {
            // 检查是否已存在测试数据
            if (hasTestData()) {
                log.info("测试数据已存在，跳过初始化");
                return;
            }

            // 创建测试用户
            for (TestUserData userData : TEST_USERS) {
                createTestUser(userData);
            }

            log.info("测试数据初始化完成，共创建 {} 个测试用户", TEST_USERS.size());
            log.info("测试账号列表：");
            for (TestUserData userData : TEST_USERS) {
                log.info("  - 手机号：{}, 用户名：{}, 密码：{}", userData.phone, userData.name, userData.password);
            }
            log.info("注意：这些账号仅绕过验证码，其他业务流程（实名认证、角色分配等）需正常进行");
        } catch (Exception e) {
            log.error("测试数据初始化失败", e);
        }
    }

    /**
     * 检查是否已存在测试数据
     */
    private boolean hasTestData() {
        for (TestUserData userData : TEST_USERS) {
            User existing = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                    .eq(User::getPhone, userData.phone)
            );
            if (existing != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 创建测试用户（只绕过验证码）
     */
    @Transactional
    public void createTestUser(TestUserData userData) {
        log.info("创建测试用户：{}", userData.phone);

        User user = new User();
        user.setUsername(userData.name);
        user.setPassword(passwordUtils.encode(userData.password));
        user.setPhone(userData.phone);
        user.setStatus(UserStatusEnum.ENABLED.getValue());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        int result = userMapper.insert(user);
        if (result > 0) {
            log.info("  - 用户创建成功，userId={}, 用户名={}", user.getId(), user.getUsername());
        } else {
            log.error("  - 用户创建失败，手机号：{}", userData.phone);
        }
    }

    /**
     * 测试用户数据
     */
    public record TestUserData(
        String phone,
        String name,      // 用户名：safe, dispatch, repair
        String password   // 统一密码：12Qw..
    ) {}
}
