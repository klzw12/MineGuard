package com.klzw.service.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRoleMapper {

    @Select("SELECT u.id FROM user u JOIN role r ON u.role_id = r.id WHERE r.role_code = #{roleCode} AND u.deleted = 0")
    List<Long> selectUserIdsByRoleCode(@Param("roleCode") String roleCode);
    
    @Select("SELECT r.role_name FROM role r WHERE r.id = #{roleId}")
    String selectRoleNameByRoleId(@Param("roleId") Long roleId);
}
