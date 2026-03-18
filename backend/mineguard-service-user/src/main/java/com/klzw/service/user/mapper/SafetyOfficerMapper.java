package com.klzw.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klzw.service.user.entity.SafetyOfficer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 安全员信息 Mapper 接口
 */
@Mapper
public interface SafetyOfficerMapper extends BaseMapper<SafetyOfficer> {

    /**
     * 根据用户ID查询安全员信息
     *
     * @param userId 用户ID
     * @return 安全员信息
     */
    @Select("SELECT * FROM safety_officer WHERE user_id = #{userId} AND deleted = 0")
    SafetyOfficer selectByUserId(@Param("userId") String userId);

    /**
     * 根据身份证号查询安全员信息
     *
     * @param idCard 身份证号
     * @return 安全员信息
     */
    @Select("SELECT * FROM safety_officer WHERE id_card = #{idCard} AND deleted = 0")
    SafetyOfficer selectByIdCard(@Param("idCard") String idCard);
}
