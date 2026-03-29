package com.klzw.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klzw.service.user.entity.RoleChangeApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    @Select("SELECT * FROM role_change_apply WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<RoleChangeApply> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询待处理的角色变更申请
     * @return 待处理的角色变更申请列表
     */
    @Select("SELECT * FROM role_change_apply WHERE status = 0 AND deleted = 0 ORDER BY create_time DESC")
    List<RoleChangeApply> selectPendingApplies();

    /**
     * 根据状态查询角色变更申请
     * @param status 状态
     * @return 角色变更申请列表
     */
    @Select("SELECT * FROM role_change_apply WHERE status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<RoleChangeApply> selectByStatus(@Param("status") Integer status);
}
