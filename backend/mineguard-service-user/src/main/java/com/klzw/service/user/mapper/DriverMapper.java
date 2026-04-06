package com.klzw.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klzw.service.user.entity.Driver;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 检查用户是否具有指定角色
     *
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 是否具有该角色
     */
    @Select("SELECT COUNT(*) > 0 FROM user u JOIN role r ON u.role_id = r.id WHERE u.id = #{userId} AND r.role_code = #{roleCode}")
    boolean hasRole(@Param("userId") Long userId, @Param("roleCode") String roleCode);

    /**
     * 查找指定时间段内已有任务的司机ID列表
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 忙碌的司机ID列表
     */
    @Select("SELECT DISTINCT executor_id FROM dispatch_task_transport " +
            "WHERE status IN (0, 1, 2) " +
            "AND scheduled_start_time >= #{startTime} " +
            "AND scheduled_start_time <= #{endTime} " +
            "AND deleted = 0")
    List<Long> findBusyDriverIds(@Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COUNT(*) FROM dispatch_task_transport " +
            "WHERE executor_id = #{driverUserId} " +
            "AND status = 3 " +
            "AND deleted = 0")
    int countCompletedTrips(@Param("driverUserId") Long driverUserId);

    @Select("SELECT COUNT(*) FROM dispatch_task_transport " +
            "WHERE executor_id = #{driverUserId} " +
            "AND deleted = 0")
    int countTotalTrips(@Param("driverUserId") Long driverUserId);
}
