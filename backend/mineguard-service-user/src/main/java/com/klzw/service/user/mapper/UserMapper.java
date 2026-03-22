package com.klzw.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klzw.service.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Update("UPDATE user SET deleted = 0 WHERE username = #{username}")
    int restoreByUsername(@Param("username") String username);

    @Update("DELETE FROM user WHERE username = #{username}")
    int physicallyDeleteByUsername(@Param("username") String username);

    @Select("SELECT r.role_code FROM user_role ur JOIN role r ON ur.role_id = r.id WHERE ur.user_id = #{userId} LIMIT 1")
    String selectRoleCodeByUserId(@Param("userId") Long userId);

}
