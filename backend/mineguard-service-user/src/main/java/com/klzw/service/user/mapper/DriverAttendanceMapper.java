package com.klzw.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klzw.service.user.entity.DriverAttendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 司机出勤记录 Mapper 接口
 */
@Mapper
public interface DriverAttendanceMapper extends BaseMapper<DriverAttendance> {

    /**
     * 根据司机ID和日期查询出勤记录
     *
     * @param driverId 司机ID
     * @param date 日期
     * @return 出勤记录
     */
    @Select("SELECT * FROM driver_attendance WHERE driver_id = #{driverId} AND attendance_date = #{date} LIMIT 1")
    DriverAttendance selectByDriverIdAndDate(@Param("driverId") Long driverId, @Param("date") LocalDate date);

    /**
     * 查询司机某月的出勤记录
     *
     * @param driverId 司机ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 出勤记录列表
     */
    @Select("SELECT * FROM driver_attendance WHERE driver_id = #{driverId} AND attendance_date BETWEEN #{startDate} AND #{endDate} ORDER BY attendance_date")
    List<DriverAttendance> selectByDriverIdAndMonth(@Param("driverId") Long driverId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计司机某月的出勤天数
     *
     * @param driverId 司机ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 出勤天数
     */
    @Select("SELECT COUNT(*) FROM driver_attendance WHERE driver_id = #{driverId} AND attendance_date BETWEEN #{startDate} AND #{endDate} AND status != 4")
    Integer countAttendanceDays(@Param("driverId") Long driverId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计司机某月的迟到次数
     *
     * @param driverId 司机ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 迟到次数
     */
    @Select("SELECT COUNT(*) FROM driver_attendance WHERE driver_id = #{driverId} AND attendance_date BETWEEN #{startDate} AND #{endDate} AND status = 2")
    Integer countLateTimes(@Param("driverId") Long driverId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计司机某月的早退次数
     *
     * @param driverId 司机ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 早退次数
     */
    @Select("SELECT COUNT(*) FROM driver_attendance WHERE driver_id = #{driverId} AND attendance_date BETWEEN #{startDate} AND #{endDate} AND status = 3")
    Integer countEarlyLeaveTimes(@Param("driverId") Long driverId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
