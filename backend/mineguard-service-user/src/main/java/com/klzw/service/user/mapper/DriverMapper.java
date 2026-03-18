package com.klzw.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klzw.service.user.entity.Driver;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 司机信息 Mapper 接口
 */
@Mapper
public interface DriverMapper extends BaseMapper<Driver> {

    /**
     * 根据用户ID查询司机信息
     *
     * @param userId 用户ID
     * @return 司机信息
     */
    @Select("SELECT * FROM driver WHERE user_id = #{userId} AND deleted = 0")
    Driver selectByUserId(@Param("userId") String userId);

    /**
     * 根据身份证号查询司机信息
     *
     * @param idCard 身份证号
     * @return 司机信息
     */
    @Select("SELECT * FROM driver WHERE id_card = #{idCard} AND deleted = 0")
    Driver selectByIdCard(@Param("idCard") String idCard);
}
