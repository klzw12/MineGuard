package com.klzw.service.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.auth.util.PasswordUtils;
import com.klzw.common.core.enums.UserStatusEnum;
import com.klzw.common.database.annotation.DataSource;
import com.klzw.service.user.config.AdminInitProperties;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.mapper.RoleMapper;
import com.klzw.service.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminInitService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordUtils passwordUtils;
    private final AdminInitProperties adminInitProperties;

    @Transactional
    @DataSource("master")
    public void initAdmin() {
        if (!adminInitProperties.isInitadmin()) {
            log.info("管理员初始化功能未开启，跳过初始化");
            return;
        }

        // 先查找管理员角色
        com.klzw.service.user.entity.Role adminRole = roleMapper.selectByRoleCode("ADMIN");
        if (adminRole == null) {
            adminRole = new com.klzw.service.user.entity.Role();
            adminRole.setRoleName("管理员");
            adminRole.setRoleCode("ADMIN");
            adminRole.setDescription("系统管理员角色");
            roleMapper.insert(adminRole);
            log.info("管理员角色创建成功");
        }

        // 检查是否已有管理员用户（根据角色ID）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRoleId, adminRole.getId());
        User existingAdmin = userMapper.selectOne(wrapper);

        if (existingAdmin != null) {
            log.info("管理员用户已存在，跳过初始化");
            return;
        }

        User admin = new User();
        admin.setUsername(adminInitProperties.getAdminUsername());
        admin.setPassword(passwordUtils.encode(adminInitProperties.getAdminPassword()));
        admin.setRealName(adminInitProperties.getAdminRealName());
        admin.setPhone(adminInitProperties.getAdminPhone());
        admin.setEmail(adminInitProperties.getAdminEmail());
        admin.setStatus(UserStatusEnum.DISABLED.getValue());
        admin.setRoleId(adminRole.getId());

        userMapper.insert(admin);
        log.info("管理员用户创建成功: {}", adminInitProperties.getAdminUsername());
        log.info("为管理员用户分配角色成功");
    }
}
