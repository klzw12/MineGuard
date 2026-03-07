package com.klzw.service.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.service.user.dto.AssignRoleDTO;
import com.klzw.service.user.dto.PasswordUpdateDTO;
import com.klzw.service.user.dto.UserRegisterDTO;
import com.klzw.service.user.dto.UserUpdateDTO;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.vo.UserVO;

import java.util.List;

public interface UserService {

    User getByUsername(String username);

    User getById(String id);

    UserVO getUserVOById(String id);

    UserVO getCurrentUser(String userId);

    UserVO register(UserRegisterDTO dto);

    UserVO updateUserInfo(String id, UserUpdateDTO dto);

    void updatePassword(String userId, PasswordUpdateDTO dto);

    Page<UserVO> pageUsers(int pageNum, int pageSize, String username, Integer status);

    void disableUser(String id);

    void enableUser(String id);

    void assignRoles(String userId, AssignRoleDTO dto);

    List<String> getRoleCodesByUserId(String userId);

    List<UserVO> getRolesByUserId(String userId);

    void clearUserCache(String userId);
}
