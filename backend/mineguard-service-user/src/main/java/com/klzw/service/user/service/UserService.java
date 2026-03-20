package com.klzw.service.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.service.user.dto.AdminCreateUserDTO;
import com.klzw.service.user.dto.PasswordUpdateDTO;
import com.klzw.service.user.dto.UserRegisterDTO;
import com.klzw.service.user.dto.UserUpdateDTO;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.vo.UserVO;

public interface UserService {

    User getByUsername(String username);

    User getById(Long id);

    UserVO getUserVOById(Long id);

    UserVO getCurrentUser(Long userId);

    UserVO register(UserRegisterDTO dto);

    UserVO updateUserInfo(Long id, UserUpdateDTO dto);

    void updatePassword(Long userId, PasswordUpdateDTO dto);

    Page<UserVO> pageUsers(int pageNum, int pageSize, String username, Integer status);

    void disableUser(Long id);

    void enableUser(Long id);

    /**
     * 分配单一角色给用户
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    void assignRole(Long userId, Long roleId);

    /**
     * 获取用户的角色编码
     * 
     * @param userId 用户ID
     * @return 角色编码
     */
    String getRoleCodeByUserId(Long userId);

    /**
     * 获取用户的角色
     * 
     * @param userId 用户ID
     * @return 角色
     */
    Role getRoleByUserId(Long userId);

    void clearUserCache(Long userId);

    /**
     * 更新用户头像
     * 
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     * @return 更新后的用户信息
     */
    UserVO updateAvatar(Long userId, String avatarUrl);

    /**
     * 根据手机号获取用户
     * 
     * @param phone 手机号
     * @return 用户
     */
    User getByPhone(String phone);

    /**
     * 创建用户
     * 
     * @param user 用户对象
     */
    void createUser(User user);

    /**
     * 管理员创建用户
     * 
     * @param dto 创建用户DTO
     * @return 用户ID
     */
    String adminCreateUser(AdminCreateUserDTO dto);

    /**
     * 更新手机号
     * 
     * @param userId 用户ID
     * @param newPhone 新手机号
     * @return 更新后的用户信息
     */
    UserVO updatePhone(Long userId, String newPhone);
    
    /**
     * 根据角色编码获取用户列表
     * 
     * @param roleCode 角色编码（可选）
     * @return 用户列表
     */
    java.util.List<UserVO> getUsersByRoleCode(String roleCode);
    
    /**
     * 获取用户头像签名URL
     * 
     * @param userId 用户ID
     * @return 签名URL，无头像返回null
     */
    String getAvatarSignedUrl(Long userId);
    
    /**
     * 获取用户身份证图片签名URL
     * 
     * @param userId 用户ID
     * @return 身份证图片信息（不包含身份证号）
     */
    com.klzw.service.user.vo.IdCardVO getIdCardSignedUrls(Long userId);
}
