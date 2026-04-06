package com.klzw.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.enums.UserStatusEnum;
import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.common.file.impl.FileUploadServiceImpl;
import com.klzw.service.user.dto.PasswordUpdateDTO;
import com.klzw.service.user.dto.UserRegisterDTO;
import com.klzw.service.user.dto.UserUpdateDTO;
import com.klzw.service.user.dto.AdminCreateUserDTO;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.exception.UserException;
import com.klzw.service.user.constant.UserResultCode;
import com.klzw.common.core.exception.BaseException;
import com.klzw.common.core.enums.ResultCodeEnum;
import com.klzw.service.user.mapper.RoleMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.UserVO;
import com.klzw.service.user.vo.IdCardVO;
import com.klzw.service.user.service.sms.SmsService;

import com.klzw.common.auth.util.PasswordUtils;
import com.klzw.common.auth.enums.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final com.klzw.service.user.mapper.UserAttendanceMapper userAttendanceMapper;
    private final PasswordUtils passwordUtils;
    private final RedisCacheService redisCacheService;
    private final FileUploadServiceImpl fileUploadService;
    private final SmsService smsService;

    private static final String USER_CACHE_PREFIX = "user:info:";
    private static final String AVATAR_CACHE_PREFIX = "avatar:signed_url:";
    private static final long USER_CACHE_EXPIRE = 30;
    private static final long AVATAR_CACHE_EXPIRE_SECONDS = 7 * 24 * 3600;

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
            throw new UserException(UserResultCode.USERNAME_EXISTS,"用户已存在");
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

        if (StringUtils.hasText(dto.getUsername()) && !dto.getUsername().equals(user.getUsername())) {
            LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
            usernameWrapper.eq(User::getUsername, dto.getUsername());
            usernameWrapper.ne(User::getId, id);
            if (userMapper.selectCount(usernameWrapper) > 0) {
                throw new UserException(UserResultCode.USERNAME_EXISTS, "用户名已被使用");
            }
            user.setUsername(dto.getUsername());
        }

        if (StringUtils.hasText(dto.getEmail())) {
            user.setEmail(dto.getEmail());
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
    public Page<UserVO> pageUsers(int pageNum, int pageSize, String keyword, Integer status) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword));
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
    @Transactional
    public void adminAssignRole(Long userId, Long roleId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND);
        }
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new UserException(UserResultCode.ROLE_NOT_FOUND);
        }

        // 检查角色是否为ADMIN或OPERATOR
        String roleCode = role.getRoleCode();
        if (!RoleEnum.ADMIN.getValue().equals(roleCode) && !RoleEnum.OPERATOR.getValue().equals(roleCode)) {
            throw new BaseException(ResultCodeEnum.PARAM_ERROR.getCode(), "只能分配管理员和调度员角色");
        }

        // 分配角色
        user.setRoleId(roleId);
        // 设置用户状态为禁用，需要实名认证
        user.setStatus(UserStatusEnum.DISABLED.getValue());
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
    public UserVO uploadAvatar(Long userId, MultipartFile file) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND);
        }

        try {
            String avatarPath = fileUploadService.upload(file, com.klzw.common.file.enums.FileBusinessTypeEnum.USER_AVATAR, String.valueOf(userId));
            user.setAvatarUrl(avatarPath);
            userMapper.updateById(user);

            clearUserCache(userId);
            return getUserVOById(userId);
        } catch (Exception e) {
            throw new UserException(UserResultCode.OPERATION_FAILED, "头像上传失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public UserVO adminCreateUser(AdminCreateUserDTO dto) {
        User existUser = getByUsername(dto.getUsername());
        if (existUser != null) {
            throw new UserException(UserResultCode.USERNAME_EXISTS);
        }

        // 验证手机号格式（如果提供了手机号）
        if (StringUtils.hasText(dto.getPhone())) {
            if (!dto.getPhone().matches("^1[3-9]\\d{9}$")) {
                throw new BaseException(ResultCodeEnum.PARAM_ERROR.getCode(), "手机号格式不正确");
            }
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, dto.getPhone());
            if (userMapper.selectCount(phoneWrapper) > 0) {
                throw new UserException(UserResultCode.PHONE_EXISTS);
            }
        }

        // 验证邮箱格式（如果提供了邮箱）
        if (StringUtils.hasText(dto.getEmail())) {
            if (!dto.getEmail().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                throw new BaseException(ResultCodeEnum.PARAM_ERROR.getCode(), "邮箱格式不正确");
            }
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordUtils.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());

        // 如果提供了角色ID，验证并分配角色
        if (dto.getRoleId() != null) {
            Role role = roleMapper.selectById(dto.getRoleId());
            if (role == null) {
                throw new UserException(UserResultCode.ROLE_NOT_FOUND);
            }

            // 检查角色是否为ADMIN或OPERATOR
            String roleCode = role.getRoleCode();
            if (!RoleEnum.ADMIN.getValue().equals(roleCode) && !RoleEnum.OPERATOR.getValue().equals(roleCode)) {
                throw new BaseException(ResultCodeEnum.PARAM_ERROR.getCode(), "只能分配管理员和调度员角色，其他角色需要通过资格认证申请");
            }

            user.setRoleId(dto.getRoleId());
            // 设置用户状态为禁用，需要实名认证
            user.setStatus(UserStatusEnum.DISABLED.getValue());
        } else {
            // 没有分配角色，设置为启用状态
            user.setStatus(UserStatusEnum.ENABLED.getValue());
        }

        userMapper.insert(user);

        return getUserVOById(user.getId());
    }

    @Override
    public UserVO updatePhone(Long userId, String newPhone, String smsCode) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND);
        }

        // 验证短信验证码
        boolean smsVerified = smsService.verifySmsCode(newPhone, smsCode);
        if (!smsVerified) {
            throw new UserException(UserResultCode.SMS_VERIFY_FAILED, "短信验证码错误或已过期");
        }

        LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(User::getPhone, newPhone);
        phoneWrapper.ne(User::getId, userId);
        if (userMapper.selectCount(phoneWrapper) > 0) {
            throw new UserException(UserResultCode.PHONE_EXISTS);
        }

        user.setPhone(newPhone);
        userMapper.updateById(user);

        clearUserCache(userId);
        return getUserVOById(userId);
    }

    @Override
    public java.util.List<UserVO> getUsersByRoleCode(String roleCode) {
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getStatus, UserStatusEnum.ENABLED.getValue());
        
        if (roleCode != null && !roleCode.isEmpty()) {
            LambdaQueryWrapper<Role> roleWrapper = new LambdaQueryWrapper<>();
            roleWrapper.eq(Role::getRoleCode, roleCode);
            Role role = roleMapper.selectOne(roleWrapper);
            if (role != null) {
                userWrapper.eq(User::getRoleId, role.getId());
            } else {
                return java.util.Collections.emptyList();
            }
        }
        
        java.util.List<User> users = userMapper.selectList(userWrapper);
        return users.stream()
                .map(this::convertToUserVO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public String getAvatarSignedUrl(Long userId) {
        String cacheKey = AVATAR_CACHE_PREFIX + userId;
        
        String cachedUrl = redisCacheService.get(cacheKey);
        if (cachedUrl != null && !cachedUrl.isEmpty()) {
            return cachedUrl;
        }
        
        User user = userMapper.selectById(userId);
        if (user == null || user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
            return null;
        }
        
        String signedUrl = fileUploadService.getSignedUrl(user.getAvatarUrl(), AVATAR_CACHE_EXPIRE_SECONDS);
        
        redisCacheService.set(cacheKey, signedUrl, AVATAR_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        
        return signedUrl;
    }

    @Override
    public IdCardVO getIdCardSignedUrls(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND);
        }
        
        if (user.getRealName() == null || user.getRealName().isEmpty()) {
            throw new BaseException(ResultCodeEnum.PARAM_ERROR.getCode(), "用户未完成实名认证");
        }
        
        IdCardVO idCardVO = new IdCardVO();
        idCardVO.setRealName(user.getRealName());
        
        if (user.getIdCardFrontUrl() != null && !user.getIdCardFrontUrl().isEmpty()) {
            idCardVO.setFrontUrl(fileUploadService.getSignedUrl(user.getIdCardFrontUrl(), 3600L));
        }
        
        if (user.getIdCardBackUrl() != null && !user.getIdCardBackUrl().isEmpty()) {
            idCardVO.setBackUrl(fileUploadService.getSignedUrl(user.getIdCardBackUrl(), 3600L));
        }
        
        return idCardVO;
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

    @Override
    public Page<UserVO> searchContacts(String keyword, String roleCode, int pageNum, int pageSize) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, UserStatusEnum.ENABLED.getValue());
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(User::getUsername, keyword)
                .or()
                .like(User::getRealName, keyword)
            );
        }
        
        if (roleCode != null && !roleCode.isEmpty()) {
            LambdaQueryWrapper<Role> roleWrapper = new LambdaQueryWrapper<>();
            roleWrapper.eq(Role::getRoleCode, roleCode);
            Role role = roleMapper.selectOne(roleWrapper);
            if (role != null) {
                wrapper.eq(User::getRoleId, role.getId());
            }
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> userPage = userMapper.selectPage(page, wrapper);
        
        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream()
            .map(this::convertToUserVO)
            .collect(java.util.stream.Collectors.toList()));
        
        return voPage;
    }

    @Override
    public Boolean existsUser(Long userId) {
        if (userId == null) {
            return false;
        }
        User user = userMapper.selectById(userId);
        return user != null && user.getDeleted() == 0;
    }

    @Override
    public java.util.List<Long> getLeaveUserIds() {
        java.time.LocalDate today = java.time.LocalDate.now();
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.klzw.service.user.entity.UserAttendance> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(com.klzw.service.user.entity.UserAttendance::getAttendanceDate, today);
        wrapper.eq(com.klzw.service.user.entity.UserAttendance::getStatus, 5); // 5表示请假
        wrapper.eq(com.klzw.service.user.entity.UserAttendance::getDeleted, 0);
        
        java.util.List<com.klzw.service.user.entity.UserAttendance> leaveRecords = userAttendanceMapper.selectList(wrapper);
        return leaveRecords.stream()
            .map(com.klzw.service.user.entity.UserAttendance::getUserId)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
    }
}
