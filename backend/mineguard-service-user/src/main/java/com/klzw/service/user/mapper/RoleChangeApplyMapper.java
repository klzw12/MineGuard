package com.klzw.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klzw.service.user.entity.RoleChangeApply;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色变更申请Mapper
 */
@Mapper
public interface RoleChangeApplyMapper extends BaseMapper<RoleChangeApply> {

    /**
     * 根据用户ID查询角色变更申请
     * @param userId 用户ID
     * @return 角色变更申请列表
     */
    List<RoleChangeApply> selectByUserId(Long userId);

    /**
     * 查询待处理的角色变更申请
     * @return 待处理的角色变更申请列表
     */
    List<RoleChangeApply> selectPendingApplies();

    /**
     * 根据状态查询角色变更申请
     * @param status 状态
     * @return 角色变更申请列表
     */
    List<RoleChangeApply> selectByStatus(Integer status);
}
