package com.klzw.common.auth.aspect;

import com.klzw.common.auth.annotation.RequirePermission;
import com.klzw.common.auth.annotation.RequireRole;
import com.klzw.common.auth.context.UserContext;
import com.klzw.common.auth.exception.AuthException;
import com.klzw.common.auth.constant.AuthResultCode;
import com.klzw.common.auth.util.PermissionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 权限切面
 */
@Aspect
@Component
public class PermissionAspect {

    /**
     * 权限检查
     *
     * @param joinPoint         连接点
     * @param requirePermission 权限注解
     */
    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new AuthException(AuthResultCode.TOKEN_MISSING, "用户未登录");
        }

        String[] permissions = requirePermission.value();
        boolean requireAll = requirePermission.requireAll();

        if (!PermissionUtils.hasPermission(userId, permissions, requireAll)) {
            throw new AuthException(AuthResultCode.PERMISSION_DENIED, "权限不足");
        }
    }

    /**
     * 角色检查
     *
     * @param joinPoint   连接点
     * @param requireRole 角色注解
     */
    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new AuthException(AuthResultCode.TOKEN_MISSING, "用户未登录");
        }

        String[] roles = requireRole.value();
        boolean requireAll = requireRole.requireAll();

        if (!PermissionUtils.hasRole(userId, roles, requireAll)) {
            throw new AuthException(AuthResultCode.ROLE_DENIED, "角色不足");
        }
    }
}
