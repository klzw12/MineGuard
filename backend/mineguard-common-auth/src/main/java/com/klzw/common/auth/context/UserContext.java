package com.klzw.common.auth.context;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户上下文
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> ROLES = new ThreadLocal<>();

    /**
     * 设置用户ID
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取用户ID
     * @return 用户ID
     */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 设置用户名
     * @param username 用户名
     */
    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    /**
     * 获取用户名
     * @return 用户名
     */
    public static String getUsername() {
        return USERNAME.get();
    }

    /**
     * 设置角色列表
     * @param roles 角色列表
     */
    public static void setRoles(List<String> roles) {
        ROLES.set(roles);
    }

    /**
     * 获取角色列表
     * @return 角色列表
     */
    public static List<String> getRoles() {
        List<String> roles = ROLES.get();
        return roles != null ? roles : new ArrayList<>();
    }

    /**
     * 检查是否拥有指定角色
     * @param role 角色名称
     * @return 是否拥有该角色
     */
    public static boolean hasRole(String role) {
        List<String> roles = getRoles();
        return roles.contains(role);
    }

    /**
     * 清除上下文
     */
    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        ROLES.remove();
    }
}
