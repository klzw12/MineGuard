package com.klzw.service.user.controller;

import com.klzw.common.auth.context.UserContext;
import com.klzw.common.core.result.Result;
import com.klzw.service.user.dto.CheckInDTO;
import com.klzw.service.user.dto.CheckOutDTO;
import com.klzw.service.user.dto.LeaveApplyDTO;
import com.klzw.service.user.service.AttendanceService;
import com.klzw.service.user.vo.AttendanceStatisticsVO;
import com.klzw.service.user.vo.AttendanceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤管理控制器
 */
@Tag(name = "考勤管理", description = "用户考勤打卡、查询、统计")
@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "上班打卡")
    @PostMapping("/check-in")
    public Result<AttendanceVO> checkIn(@RequestBody CheckInDTO dto) {
        AttendanceVO vo = attendanceService.checkIn(dto);
        return Result.success(vo);
    }

    @Operation(summary = "下班打卡")
    @PostMapping("/check-out")
    public Result<AttendanceVO> checkOut(@RequestBody CheckOutDTO dto) {
        AttendanceVO vo = attendanceService.checkOut(dto);
        return Result.success(vo);
    }

    @Operation(summary = "获取某日考勤记录")
    @GetMapping("/date")
    public Result<AttendanceVO> getAttendanceByDate(
            @Parameter(description = "日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = UserContext.getUserId();
        AttendanceVO vo = attendanceService.getAttendanceByDate(userId, date);
        return Result.success(vo);
    }

    @Operation(summary = "获取某月考勤记录列表")
    @GetMapping("/list")
    public Result<List<AttendanceVO>> getAttendanceListByMonth(
            @Parameter(description = "年月(yyyy-MM)") @RequestParam String yearMonth) {
        Long userId = UserContext.getUserId();
        List<AttendanceVO> list = attendanceService.getAttendanceListByMonth(userId, yearMonth);
        return Result.success(list);
    }

    @Operation(summary = "获取某月考勤统计")
    @GetMapping("/statistics")
    public Result<AttendanceStatisticsVO> getAttendanceStatistics(
            @Parameter(description = "年月(yyyy-MM)") @RequestParam String yearMonth) {
        Long userId = UserContext.getUserId();
        AttendanceStatisticsVO statistics = attendanceService.getAttendanceStatistics(userId, yearMonth);
        return Result.success(statistics);
    }

    @Operation(summary = "获取日期范围考勤统计")
    @GetMapping("/statistics/range")
    public Result<AttendanceStatisticsVO> getAttendanceStatisticsByRange(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = UserContext.getUserId();
        AttendanceStatisticsVO statistics = attendanceService.getAttendanceStatisticsByDateRange(userId, startDate, endDate);
        return Result.success(statistics);
    }

    @Operation(summary = "补卡（管理员功能）")
    @PutMapping("/{attendanceId}/supplement")
    public Result<AttendanceVO> supplementAttendance(
            @Parameter(description = "出勤记录ID") @PathVariable Long attendanceId,
            @Parameter(description = "上班时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkInTime,
            @Parameter(description = "下班时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOutTime,
            @Parameter(description = "状态(1-正常,2-迟到,3-早退,4-缺勤)") @RequestParam(required = false) Integer status,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        AttendanceVO vo = attendanceService.supplementAttendance(attendanceId, checkInTime, checkOutTime, status, remark);
        return Result.success(vo);
    }

    @Operation(summary = "请假申请")
    @PostMapping("/leave")
    public Result<AttendanceVO> applyLeave(
            @Parameter(description = "请假信息") @RequestBody LeaveApplyDTO dto) {
        Long userId = UserContext.getUserId();
        AttendanceVO vo = attendanceService.applyLeave(userId, dto.getLeaveType(), dto.getStartTime(), dto.getEndTime(), dto.getReason());
        return Result.success(vo);
    }

    @Operation(summary = "取消请假")
    @DeleteMapping("/leave/{attendanceId}")
    public Result<Boolean> cancelLeave(@PathVariable Long attendanceId) {
        return Result.success(attendanceService.cancelLeave(attendanceId));
    }

    @Operation(summary = "获取请假记录列表")
    @GetMapping("/leave-list")
    public Result<List<AttendanceVO>> getLeaveList(
            @Parameter(description = "年月(yyyy-MM)") @RequestParam String yearMonth) {
        Long userId = UserContext.getUserId();
        return Result.success(attendanceService.getLeaveList(userId, yearMonth));
    }

    @Operation(summary = "获取可用司机ID列表")
    @GetMapping("/available-drivers")
    public Result<List<Long>> getAvailableDriverIds() {
        return Result.success(attendanceService.getAvailableDriverIds());
    }

    @Operation(summary = "内部接口：获取用户出勤统计")
    @GetMapping("/statistics/internal")
    public Result<AttendanceStatisticsVO> getAttendanceStatisticsInternal(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        AttendanceStatisticsVO statistics = attendanceService.getAttendanceStatisticsByDateRange(userId, startDate, endDate);
        return Result.success(statistics);
    }
}
