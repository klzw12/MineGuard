package com.klzw.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klzw.service.user.entity.Repairman;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 维修员信息 Mapper 接口
 */
@Mapper
public interface RepairmanMapper extends BaseMapper<Repairman> {

    /**
     * 根据用户ID查询维修员信息
     *
     * @param userId 用户ID
     * @return 维修员信息
     */
    @Select("SELECT * FROM repairman WHERE user_id = #{userId} AND deleted = 0")
    Repairman selectByUserId(@Param("userId") String userId);

    /**
     * 根据身份证号查询维修员信息
     *
     * @param idCard 身份证号
     * @return 维修员信息
     */
    @Select("SELECT * FROM repairman WHERE id_card = #{idCard} AND deleted = 0")
    Repairman selectByIdCard(@Param("idCard") String idCard);
}
