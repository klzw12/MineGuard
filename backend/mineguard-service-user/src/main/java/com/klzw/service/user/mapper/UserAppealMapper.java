package com.klzw.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klzw.service.user.entity.UserAppeal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户申诉Mapper接口
 */
@Mapper
public interface UserAppealMapper extends BaseMapper<UserAppeal> {

    /**
     * 根据用户ID查询申诉列表
     */
    @Select("SELECT * FROM user_appeal WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<UserAppeal> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询待处理的申诉列表
     */
    @Select("SELECT * FROM user_appeal WHERE status = 1 ORDER BY create_time ASC")
    List<UserAppeal> selectPendingApplies();

    /**
     * 查询用户是否有待处理的申诉
     */
    @Select("SELECT COUNT(*) FROM user_appeal WHERE user_id = #{userId} AND status = 1")
    int countPendingByUserId(@Param("userId") Long userId);
}
