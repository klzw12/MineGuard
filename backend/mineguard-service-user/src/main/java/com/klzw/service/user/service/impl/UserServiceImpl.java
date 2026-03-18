package com.klzw.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.enums.UserStatusEnum;
import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.service.user.dto.PasswordUpdateDTO;
import com.klzw.service.user.dto.UserRegisterDTO;
import com.klzw.service.user.dto.UserUpdateDTO;
import com.klzw.service.user.dto.AdminCreateUserDTO;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.exception.UserException;
import com.klzw.service.user.constant.UserResultCode;
import com.klzw.service.user.mapper.RoleMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.UserVO;

import com.klzw.common.auth.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
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
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public UserVO getUserVOById(Long id) {
        UserVO cachedUser = redisCacheService.get(USER_CACHE_PREFIX + id);
        if (cachedUser != null) {
            return cachedUser;
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            return null;
        }

        UserVO userVO = convertToUserVO(user);
        String roleCode = getRoleCodeByUserId(id);
        userVO.setRoleCode(roleCode);

        redisCacheService.set(USER_CACHE_PREFIX + id, userVO, USER_CACHE_EXPIRE, TimeUnit.MINUTES);
        return userVO;
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        return getUserVOById(userId);
    }

    @Override
    @Transactional
    public UserVO register(UserRegisterDTO dto) {
        User existUser = getByUsername(dto.getUsername());
        if (existUser != null) {
            throw new UserException(UserResultCode.USERNAME_EXISTS);
        }

        if (StringUtils.hasText(dto.getPhone())) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, dto.getPhone());
            if (userMapper.selectCount(phoneWrapper) > 0) {
                throw new UserException(UserResultCode.PHONE_EXISTS);
            }
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordUtils.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(UserStatusEnum.DISABLED.getValue());

        userMapper.insert(user);

        return convertToUserVO(user);
    }

    @Override
    @Transactional
    public UserVO updateUserInfo(Long id, UserUpdateDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND);
        }

        if (StringUtils.hasText(dto.getPhone()) && !dto.getPhone().equals(user.getPhone())) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, dto.getPhone());
            phoneWrapper.ne(User::getId, id);
            if (userMapper.selectCount(phoneWrapper) > 0) {
                throw new UserException(UserResultCode.PHONE_EXISTS, "手机号已被使用");
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

        userMapper.updateById(user);
        clearUserCache(id);

        return convertToUserVO(user);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, PasswordUpdateDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND);
        }

        if (!passwordUtils.matches(dto.getOldPassword(), user.getPassword())) {
            throw new UserException(UserResultCode.OLD_PASSWORD_ERROR);
        }

        user.setPassword(passwordUtils.encode(dto.getNewPassword()));
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
    public void disableUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND);
        }

        user.setStatus(UserStatusEnum.DISABLED.getValue());
        userMapper.updateById(user);

        clearUserCache(id);
    }

    @Override
    @Transactional
    public void enableUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND);
        }

        user.setStatus(UserStatusEnum.ENABLED.getValue());
        userMapper.updateById(user);

        clearUserCache(id);
    }

    @Override
    @Transactional
    public void assignRole(Long userId, Long roleId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND);
        }

        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new UserException(UserResultCode.ROLE_NOT_FOUND);
        }

        user.setRoleId(roleId);
        userMapper.updateById(user);

        clearUserCache(userId);
    }

    @Override
    public String getRoleCodeByUserId(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getRoleId() == null) {
            return null;
        }
        Role role = roleMapper.selectById(user.getRoleId());
        return role != null ? role.getRoleCode() : null;
    }

    @Override
    public Role getRoleByUserId(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getRoleId() == null) {
            return null;
        }
        return roleMapper.selectById(user.getRoleId());
    }

    @Override
    public void clearUserCache(Long userId) {
        redisCacheService.delete(USER_CACHE_PREFIX + userId);
    }

    @Override
    public User getByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public void createUser(User user) {
        userMapper.insert(user);
    }

    @Override
    public UserVO updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND);
        }

        user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);

        clearUserCache(userId);
        return getUserVOById(userId);
    }

    @Override
    @Transactional
    public String adminCreateUser(AdminCreateUserDTO dto) {
        User existUser = getByUsername(dto.getUsername());
        if (existUser != null) {
            throw new UserException(UserResultCode.USERNAME_EXISTS);
        }

        if (StringUtils.hasText(dto.getPhone())) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, dto.getPhone());
            if (userMapper.selectCount(phoneWrapper) > 0) {
                throw new UserException(UserResultCode.PHONE_EXISTS);
            }
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordUtils.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(UserStatusEnum.ENABLED.getValue());

        userMapper.insert(user);

        return String.valueOf(user.getId());
    }

    private UserVO convertToUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        if (user.getId() != null) {
            vo.setId(user.getId().toString());
        }
        if (user.getRoleId() != null) {
            vo.setRoleId(user.getRoleId().toString());
            Role role = roleMapper.selectById(user.getRoleId());
            if (role != null) {
                vo.setRoleCode(role.getRoleCode());
                vo.setRoleName(role.getRoleName());
            }
        }
        return vo;
    }
}
