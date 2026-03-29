package com.klzw.service.user.config;

import com.klzw.service.user.entity.Role;
import com.klzw.service.user.mapper.RoleMapper;
import com.klzw.common.core.enums.UserTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色初始化器
 * 在生产环境下自动初始化系统基础角色
 * 通过 mineguard.init.data.role 控制是否启用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final RoleMapper roleMapper;

    @Value("${mineguard.init.data.role:false}")
    private boolean initRoleEnable;

    // 系统基础角色描述
    private static final java.util.Map<UserTypeEnum, String> ROLE_DESCRIPTIONS = java.util.Map.of(
        UserTypeEnum.ADMIN, "系统管理员，拥有所有权限",
        UserTypeEnum.DRIVER, "司机角色，负责车辆驾驶",
        UserTypeEnum.OPERATOR, "运营人员角色，处理调度工作等，分担管理员的业务",
        UserTypeEnum.REPAIRMAN, "维修员角色，负责车辆维修",
        UserTypeEnum.SAFETY_OFFICER, "安全员角色，负责安全监督"
    );

    @Override
    @Transactional
    public void run(String... args) {
        // 检查是否启用初始化
        if (!initRoleEnable) {
            log.debug("角色初始化未启用，设置 mineguard.init.data.role=true 以启用");
            return;
        }
        
        log.info("开始初始化系统角色...");
        
        try {
            int createdCount = 0;
            int existingCount = 0;
            
            for (UserTypeEnum userType : UserTypeEnum.values()) {
                String description = ROLE_DESCRIPTIONS.get(userType);
                if (initRole(userType.getLabel(), userType.getRoleCode(), description)) {
                    createdCount++;
                } else {
                    existingCount++;
                }
            }
            
            log.info("角色初始化完成：创建 {} 个角色，跳过 {} 个已存在角色", createdCount, existingCount);
        } catch (Exception e) {
            log.error("角色初始化失败", e);
        }
    }

    /**
     * 初始化角色
     */
    private boolean initRole(String roleName, String roleCode, String description) {
        // 检查角色是否已存在
        Role existingRole = roleMapper.selectByRoleCode(roleCode);
        if (existingRole != null) {
            log.debug("角色已存在，跳过：{}({})", roleName, roleCode);
            return false;
        }
        
        // 创建新角色
        Role role = new Role();
        role.setRoleName(roleName);
        role.setRoleCode(roleCode);
        role.setDescription(description);
        
        int result = roleMapper.insert(role);
        if (result > 0) {
            log.info("角色创建成功：{}({})", roleName, roleCode);
            return true;
        } else {
            log.error("角色创建失败：{}({})", roleName, roleCode);
            return false;
        }
    }


}