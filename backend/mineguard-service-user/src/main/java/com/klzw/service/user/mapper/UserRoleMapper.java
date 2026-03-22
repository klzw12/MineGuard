package com.klzw.service.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRoleMapper {

    @Select("SELECT ur.user_id FROM user_role ur JOIN role r ON ur.role_id = r.id WHERE r.role_code = #{roleCode}")
    List<Long> selectUserIdsByRoleCode(@Param("roleCode") String roleCode);
}
