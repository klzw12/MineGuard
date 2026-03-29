package com.klzw.service.user.service.impl;

import com.klzw.service.user.entity.RoleChangeApply;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.mapper.RoleChangeApplyMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.service.RoleChangeApplyService;
import com.klzw.service.user.service.UserService;
import com.klzw.service.user.vo.RoleChangeApplyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色变更申请服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleChangeApplyServiceImpl implements RoleChangeApplyService {

    private final RoleChangeApplyMapper roleChangeApplyMapper;
    private final UserMapper userMapper;
    private final UserService userService;

    @Override
    @Transactional
    public String createRoleChangeApply(RoleChangeApply apply) {
        log.info("创建角色变更申请，用户ID：{}，申请角色：{}", apply.getUserId(), apply.getApplyRoleName());

        // 设置默认值
        apply.setStatus(1); // 1-待处理
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());

        // 保存申请
        roleChangeApplyMapper.insert(apply);

        log.info("角色变更申请创建成功，申请ID：{}", apply.getId());
        return String.valueOf(apply.getId());
    }

    @Override
    public RoleChangeApply getRoleChangeApplyById(Long id) {
        return roleChangeApplyMapper.selectById(id);
    }

    @Override
    public List<RoleChangeApplyVO> getRoleChangeAppliesByUserId(Long userId) {
        List<RoleChangeApply> applies = roleChangeApplyMapper.selectByUserId(userId);
        return applies.stream()
                .map(RoleChangeApplyVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoleChangeApplyVO> getPendingRoleChangeApplies() {
        List<RoleChangeApply> applies = roleChangeApplyMapper.selectPendingApplies();
        return applies.stream()
                .map(RoleChangeApplyVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean handleRoleChangeApply(Long id, Integer status, String adminOpinion, Long handlerId, String handlerName) {
        log.info("处理角色变更申请，申请ID：{}，处理状态：{}，处理人：{}", id, status, handlerName);

        // 查询申请
        RoleChangeApply apply = roleChangeApplyMapper.selectById(id);
        if (apply == null) {
            log.error("角色变更申请不存在，申请ID：{}", id);
            return false;
        }

        // 检查申请状态
        if (apply.getStatus() != 1) {
            log.error("角色变更申请已处理，申请ID：{}", id);
            return false;
        }

        // 更新申请状态
        apply.setStatus(status);
        apply.setAdminOpinion(adminOpinion);
        apply.setHandleTime(LocalDateTime.now());
        apply.setHandlerId(handlerId);
        apply.setHandlerName(handlerName);
        apply.setUpdateTime(LocalDateTime.now());

        roleChangeApplyMapper.updateById(apply);

        // 如果申请通过，更新用户角色
        if (status == 2) {
            User user = userMapper.selectById(apply.getUserId());
            if (user != null) {
                user.setRoleId(apply.getApplyRoleId());
                user.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(user);
                // 清除用户缓存
                userService.clearUserCache(user.getId());
                log.info("用户角色已更新，用户ID：{}，新角色：{}", user.getId(), apply.getApplyRoleName());
            }
        }

        log.info("角色变更申请处理成功，申请ID：{}", id);
        return true;
    }

    @Override
    public List<RoleChangeApplyVO> getAllRoleChangeApplies() {
        List<RoleChangeApply> applies = roleChangeApplyMapper.selectList(null);
        return applies.stream()
                .map(RoleChangeApplyVO::fromEntity)
                .collect(Collectors.toList());
    }
}
