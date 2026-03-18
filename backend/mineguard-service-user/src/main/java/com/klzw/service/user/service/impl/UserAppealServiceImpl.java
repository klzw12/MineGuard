package com.klzw.service.user.service.impl;

import com.klzw.common.core.enums.UserStatusEnum;
import com.klzw.service.user.dto.HandleAppealDTO;
import com.klzw.service.user.dto.UserAppealDTO;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.entity.UserAppeal;
import com.klzw.service.user.mapper.UserAppealMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.service.UserAppealService;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.UserAppealVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户申诉服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAppealServiceImpl implements UserAppealService {

    private final UserAppealMapper userAppealMapper;
    private final UserMapper userMapper;
    private final UserService userService;

    @Override
    @Transactional
    public String createAppeal(Long userId, UserAppealDTO dto) {
        log.info("创建用户申诉，用户ID：{}", userId);

        // 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户是否已被禁用
        if (user.getStatus() != UserStatusEnum.DISABLED.getValue()) {
            throw new RuntimeException("用户账号未被禁用，无需申诉");
        }

        // 检查是否已有待处理的申诉
        if (userAppealMapper.countPendingByUserId(userId) > 0) {
            throw new RuntimeException("您已有一个待处理的申诉，请勿重复提交");
        }

        // 创建申诉
        UserAppeal appeal = new UserAppeal();
        appeal.setUserId(userId);
        appeal.setUsername(user.getUsername());
        appeal.setRealName(user.getRealName());
        appeal.setPhone(user.getPhone());
        appeal.setAppealReason(dto.getAppealReason());
        appeal.setStatus(1); // 1-待处理
        appeal.setCreateTime(LocalDateTime.now());
        appeal.setUpdateTime(LocalDateTime.now());

        userAppealMapper.insert(appeal);

        log.info("用户申诉创建成功，申诉ID：{}", appeal.getId());
        return String.valueOf(appeal.getId());
    }

    @Override
    public UserAppealVO getAppealById(Long id) {
        UserAppeal appeal = userAppealMapper.selectById(id);
        return UserAppealVO.fromEntity(appeal);
    }

    @Override
    public List<UserAppealVO> getAppealsByUserId(Long userId) {
        List<UserAppeal> appeals = userAppealMapper.selectByUserId(userId);
        return appeals.stream()
                .map(UserAppealVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserAppealVO> getPendingAppeals() {
        List<UserAppeal> appeals = userAppealMapper.selectPendingApplies();
        return appeals.stream()
                .map(UserAppealVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserAppealVO> getAllAppeals() {
        List<UserAppeal> appeals = userAppealMapper.selectList(null);
        return appeals.stream()
                .map(UserAppealVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean handleAppeal(Long id, HandleAppealDTO dto) {
        log.info("处理用户申诉，申诉ID：{}，处理状态：{}，处理人：{}", id, dto.getStatus(), dto.getHandlerName());

        // 查询申诉
        UserAppeal appeal = userAppealMapper.selectById(id);
        if (appeal == null) {
            log.error("申诉不存在，申诉ID：{}", id);
            return false;
        }

        // 检查申诉状态
        if (appeal.getStatus() != 1) {
            log.error("申诉已处理，申诉ID：{}", id);
            return false;
        }

        // 更新申诉状态
        appeal.setStatus(dto.getStatus());
        appeal.setAdminOpinion(dto.getAdminOpinion());
        appeal.setHandleTime(LocalDateTime.now());
        appeal.setHandlerId(dto.getHandlerId());
        appeal.setHandlerName(dto.getHandlerName());
        appeal.setUpdateTime(LocalDateTime.now());

        userAppealMapper.updateById(appeal);

        // 根据处理状态执行相应操作
        Integer status = dto.getStatus();
        Long userId = appeal.getUserId();

        if (status == 2) {
            // 已通过 - 解除禁用
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setStatus(UserStatusEnum.ENABLED.getValue());
                user.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(user);
                userService.clearUserCache(userId);
                log.info("用户账号已解除禁用，用户ID：{}", userId);
            }
        } else if (status == 4) {
            // 已驳回并删除账号 - 逻辑删除用户
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setDeleted(1);
                user.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(user);
                userService.clearUserCache(userId);
                log.info("用户账号已删除，用户ID：{}", userId);
            }
        }

        log.info("用户申诉处理成功，申诉ID：{}", id);
        return true;
    }

    @Override
    public boolean hasPendingAppeal(Long userId) {
        return userAppealMapper.countPendingByUserId(userId) > 0;
    }
}
