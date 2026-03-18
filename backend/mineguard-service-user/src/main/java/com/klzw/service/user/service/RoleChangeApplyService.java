package com.klzw.service.user.service;

import com.klzw.service.user.entity.RoleChangeApply;
import com.klzw.service.user.vo.RoleChangeApplyVO;

import java.util.List;

/**
 * 角色变更申请服务接口
 */
public interface RoleChangeApplyService {

    /**
     * 创建角色变更申请
     * @param apply 角色变更申请
     * @return 申请ID
     */
    String createRoleChangeApply(RoleChangeApply apply);

    /**
     * 根据ID查询角色变更申请
     * @param id 申请ID
     * @return 角色变更申请
     */
    RoleChangeApply getRoleChangeApplyById(Long id);

    /**
     * 根据用户ID查询角色变更申请
     * @param userId 用户ID
     * @return 角色变更申请列表
     */
    List<RoleChangeApplyVO> getRoleChangeAppliesByUserId(Long userId);

    /**
     * 查询待处理的角色变更申请
     * @return 待处理的角色变更申请列表
     */
    List<RoleChangeApplyVO> getPendingRoleChangeApplies();

    /**
     * 处理角色变更申请
     * @param id 申请ID
     * @param status 处理状态：2-已通过，3-已拒绝
     * @param adminOpinion 管理员处理意见
     * @param handlerId 处理人ID
     * @param handlerName 处理人姓名
     * @return 处理结果
     */
    boolean handleRoleChangeApply(Long id, Integer status, String adminOpinion, Long handlerId, String handlerName);

    /**
     * 查询所有角色变更申请
     * @return 角色变更申请列表
     */
    List<RoleChangeApplyVO> getAllRoleChangeApplies();
}
