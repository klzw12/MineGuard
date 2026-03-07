package com.klzw.common.auth.aspect;

import com.klzw.common.auth.annotation.RequirePermission;
import com.klzw.common.auth.annotation.RequireRole;
import com.klzw.common.auth.context.UserContext;
import com.klzw.common.auth.constant.AuthResultCode;
import com.klzw.common.auth.exception.AuthException;
import com.klzw.common.auth.util.PermissionUtils;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 权限切面单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PermissionAspectTest {

    private PermissionAspect permissionAspect;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private RequirePermission requirePermission;

    @Mock
    private RequireRole requireRole;

    @BeforeEach
    void setUp() {
        permissionAspect = new PermissionAspect();
        // 设置权限和角色提供者
        PermissionUtils.setPermissionProvider(uid -> 
            Arrays.asList("user:read", "user:write", "user:delete")
        );
        PermissionUtils.setRoleProvider(uid -> 
            Arrays.asList("ROLE_USER", "ROLE_ADMIN")
        );
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
        PermissionUtils.setPermissionProvider(null);
        PermissionUtils.setRoleProvider(null);
    }

    @Test
    void checkPermission_shouldPass_whenUserHasAllPermissions() {
        Long userId = 1L;
        UserContext.setUserId(userId);
        
        String[] permissions = {"user:read", "user:write"};
        when(requirePermission.value()).thenReturn(permissions);
        when(requirePermission.requireAll()).thenReturn(true);

        assertDoesNotThrow(() -> permissionAspect.checkPermission(joinPoint, requirePermission));
    }

    @Test
    void checkPermission_shouldThrowException_whenUserMissingPermissions() {
        Long userId = 1L;
        UserContext.setUserId(userId);
        
        String[] permissions = {"admin:read", "admin:write"};
        when(requirePermission.value()).thenReturn(permissions);
        when(requirePermission.requireAll()).thenReturn(true);

        AuthException exception = assertThrows(AuthException.class, () -> {
            permissionAspect.checkPermission(joinPoint, requirePermission);
        });

        assertEquals(AuthResultCode.PERMISSION_DENIED.getCode(), exception.getCode());
    }

    @Test
    void checkPermission_shouldThrowException_whenUserNotLoggedIn() {
        // 不设置用户ID，模拟未登录状态
        String[] permissions = {"user:read"};
        when(requirePermission.value()).thenReturn(permissions);
        when(requirePermission.requireAll()).thenReturn(true);

        AuthException exception = assertThrows(AuthException.class, () -> {
            permissionAspect.checkPermission(joinPoint, requirePermission);
        });

        assertEquals(AuthResultCode.TOKEN_MISSING.getCode(), exception.getCode());
    }

    @Test
    void checkPermission_shouldPass_whenUserHasAnyPermission() {
        Long userId = 1L;
        UserContext.setUserId(userId);
        
        String[] permissions = {"user:read", "admin:read"};
        when(requirePermission.value()).thenReturn(permissions);
        when(requirePermission.requireAll()).thenReturn(false);

        assertDoesNotThrow(() -> permissionAspect.checkPermission(joinPoint, requirePermission));
    }

    @Test
    void checkRole_shouldPass_whenUserHasAllRoles() {
        Long userId = 1L;
        UserContext.setUserId(userId);
        
        String[] roles = {"ROLE_USER", "ROLE_ADMIN"};
        when(requireRole.value()).thenReturn(roles);
        when(requireRole.requireAll()).thenReturn(true);

        assertDoesNotThrow(() -> permissionAspect.checkRole(joinPoint, requireRole));
    }

    @Test
    void checkRole_shouldThrowException_whenUserMissingRoles() {
        Long userId = 1L;
        UserContext.setUserId(userId);
        
        String[] roles = {"ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN"};
        when(requireRole.value()).thenReturn(roles);
        when(requireRole.requireAll()).thenReturn(true);

        AuthException exception = assertThrows(AuthException.class, () -> {
            permissionAspect.checkRole(joinPoint, requireRole);
        });

        assertEquals(AuthResultCode.ROLE_DENIED.getCode(), exception.getCode());
    }

    @Test
    void checkRole_shouldThrowException_whenUserNotLoggedIn() {
        // 不设置用户ID，模拟未登录状态
        String[] roles = {"ROLE_USER"};
        when(requireRole.value()).thenReturn(roles);
        when(requireRole.requireAll()).thenReturn(true);

        AuthException exception = assertThrows(AuthException.class, () -> {
            permissionAspect.checkRole(joinPoint, requireRole);
        });

        assertEquals(AuthResultCode.TOKEN_MISSING.getCode(), exception.getCode());
    }

    @Test
    void checkRole_shouldPass_whenUserHasAnyRole() {
        Long userId = 1L;
        UserContext.setUserId(userId);
        
        String[] roles = {"ROLE_ADMIN", "ROLE_SUPER_ADMIN"};
        when(requireRole.value()).thenReturn(roles);
        when(requireRole.requireAll()).thenReturn(false);

        assertDoesNotThrow(() -> permissionAspect.checkRole(joinPoint, requireRole));
    }
}