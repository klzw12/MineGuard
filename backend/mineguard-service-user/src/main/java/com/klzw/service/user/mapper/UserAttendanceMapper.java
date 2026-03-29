package com.klzw.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klzw.service.user.entity.UserAttendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户考勤记录 Mapper 接口
 */
@Mapper
public interface UserAttendanceMapper extends BaseMapper<UserAttendance> {

    /**
     * 根据用户ID和日期查询考勤记录
     *
     * @param userId 用户ID
     * @param date 日期
     * @return 考勤记录
     */
    @Select("SELECT * FROM user_attendance WHERE user_id = #{userId} AND attendance_date = #{date} AND deleted = 0 LIMIT 1")
    UserAttendance selectByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 查询用户某月的考勤记录
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 考勤记录列表
     */
    @Select("SELECT * FROM user_attendance WHERE user_id = #{userId} AND attendance_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0 ORDER BY attendance_date")
    List<UserAttendance> selectByUserIdAndMonth(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计用户某月的出勤天数
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 出勤天数
     */
    @Select("SELECT COUNT(*) FROM user_attendance WHERE user_id = #{userId} AND attendance_date BETWEEN #{startDate} AND #{endDate} AND status != 4 AND deleted = 0")
    Integer countAttendanceDays(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计用户某月的迟到次数
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 迟到次数
     */
    @Select("SELECT COUNT(*) FROM user_attendance WHERE user_id = #{userId} AND attendance_date BETWEEN #{startDate} AND #{endDate} AND status = 2 AND deleted = 0")
    Integer countLateTimes(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计用户某月的早退次数
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 早退次数
     */
    @Select("SELECT COUNT(*) FROM user_attendance WHERE user_id = #{userId} AND attendance_date BETWEEN #{startDate} AND #{endDate} AND status = 3 AND deleted = 0")
    Integer countEarlyLeaveTimes(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COUNT(*) FROM user_attendance WHERE user_id = #{userId} AND attendance_date BETWEEN #{startDate} AND #{endDate} AND status = 1 AND deleted = 0")
    Integer countNormalDays(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COUNT(*) FROM user_attendance WHERE user_id = #{userId} AND attendance_date BETWEEN #{startDate} AND #{endDate} AND status = 5 AND deleted = 0")
    Integer countLeaveDays(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COUNT(*) FROM user_attendance WHERE user_id = #{userId} AND attendance_date BETWEEN #{startDate} AND #{endDate} AND status IN (2, 6) AND deleted = 0")
    Integer countLateAndEarlyLeaveDays(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
