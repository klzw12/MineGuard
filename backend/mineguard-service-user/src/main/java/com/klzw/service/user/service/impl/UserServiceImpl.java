package com.klzw.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.exception.BusinessException;
import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.service.user.dto.AssignRoleDTO;
import com.klzw.service.user.dto.PasswordUpdateDTO;
import com.klzw.service.user.dto.UserRegisterDTO;
import com.klzw.service.user.dto.UserUpdateDTO;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.entity.UserRole;
import com.klzw.service.user.mapper.RoleMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.mapper.UserRoleMapper;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.UserVO;
import com.klzw.common.auth.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordUtils passwordUtils;
    private final RedisCacheService redisCacheService;

    private static final String USER_CACHE_PREFIX = "user:info:";
    private static final long USER_CACHE_EXPIRE = 30;

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public User getById(String id) {
        return userMapper.selectById(id);
    }

    @Override
    public UserVO getUserVOById(String id) {
        UserVO cachedUser = redisCacheService.get(USER_CACHE_PREFIX + id);
        if (cachedUser != null) {
            return cachedUser;
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            return null;
        }

        UserVO userVO = convertToUserVO(user);
        List<Role> roles = userMapper.selectRolesByUserId(id);
        userVO.setRoles(roles.stream().map(Role::getRoleCode).collect(Collectors.toList()));

        redisCacheService.set(USER_CACHE_PREFIX + id, userVO, USER_CACHE_EXPIRE, TimeUnit.MINUTES);
        return userVO;
    }

    @Override
    public UserVO getCurrentUser(String userId) {
        return getUserVOById(userId);
    }

    @Override
    @Transactional
    public UserVO register(UserRegisterDTO dto) {
        User existUser = getByUsername(dto.getUsername());
        if (existUser != null) {
            throw new BusinessException(400, "用户名已存在");
        }

        if (StringUtils.hasText(dto.getPhone())) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, dto.getPhone());
            if (userMapper.selectCount(phoneWrapper) > 0) {
                throw new BusinessException(400, "手机号已被注册");
            }
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordUtils.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(1);
        user.setUserType(1);
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);

        return convertToUserVO(user);
    }

    @Override
    @Transactional
    public UserVO updateUserInfo(String id, UserUpdateDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (StringUtils.hasText(dto.getPhone()) && !dto.getPhone().equals(user.getPhone())) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, dto.getPhone());
            phoneWrapper.ne(User::getId, id);
            if (userMapper.selectCount(phoneWrapper) > 0) {
                throw new BusinessException(400, "手机号已被使用");
            }
        }

        if (StringUtils.hasText(dto.getEmail())) {
            user.setEmail(dto.getEmail());
        }
        if (StringUtils.hasText(dto.getPhone())) {
            user.setPhone(dto.getPhone());
        }
        if (StringUtils.hasText(dto.getRealName())) {
            user.setRealName(dto.getRealName());
        }
        if (StringUtils.hasText(dto.getAvatarUrl())) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);
        clearUserCache(id);

        return convertToUserVO(user);
    }

    @Override
    @Transactional
    public void updatePassword(String userId, PasswordUpdateDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (!passwordUtils.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }

        user.setPassword(passwordUtils.encode(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        clearUserCache(userId);
    }

    @Override
    public Page<UserVO> pageUsers(int pageNum, int pageSize, String username, Integer status) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> userPage = userMapper.selectPage(page, wrapper);

        Page<UserVO> resultPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> userVOList = userPage.getRecords().stream()
                .map(this::convertToUserVO)
                .collect(Collectors.toList());
        resultPage.setRecords(userVOList);

        return resultPage;
    }

    @Override
    @Transactional
    public void disableUser(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        user.setStatus(0);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        clearUserCache(id);
    }

    @Override
    @Transactional
    public void enableUser(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        user.setStatus(1);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        clearUserCache(id);
    }

    @Override
    @Transactional
    public void assignRoles(String userId, AssignRoleDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        userRoleMapper.deleteByUserId(userId);

        for (String roleId : dto.getRoleIds()) {
            Role role = roleMapper.selectById(roleId);
            if (role != null) {
                UserRole userRole = new UserRole();
                userRole.setId(UUID.randomUUID().toString());
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setCreateTime(LocalDateTime.now());
                userRoleMapper.insert(userRole);
            }
        }

        clearUserCache(userId);
    }

    @Override
    public List<String> getRoleCodesByUserId(String userId) {
        return userMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    public List<UserVO> getRolesByUserId(String userId) {
        List<Role> roles = userMapper.selectRolesByUserId(userId);
        return roles.stream().map(role -> {
            UserVO vo = new UserVO();
            vo.setId(role.getId());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void clearUserCache(String userId) {
        redisCacheService.delete(USER_CACHE_PREFIX + userId);
    }

    private UserVO convertToUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
