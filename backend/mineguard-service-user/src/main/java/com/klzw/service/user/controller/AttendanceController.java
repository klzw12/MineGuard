package com.klzw.service.user.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.user.dto.CheckInDTO;
import com.klzw.service.user.dto.CheckOutDTO;
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
import java.util.List;

/**
 * 考勤管理控制器
 */
@Tag(name = "考勤管理", description = "司机考勤打卡、查询、统计")
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
    @GetMapping("/{driverId}")
    public Result<AttendanceVO> getAttendanceByDate(
            @Parameter(description = "司机ID") @PathVariable Long driverId,
            @Parameter(description = "日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        AttendanceVO vo = attendanceService.getAttendanceByDate(driverId, date);
        return Result.success(vo);
    }

    @Operation(summary = "获取某月考勤记录列表")
    @GetMapping("/{driverId}/list")
    public Result<List<AttendanceVO>> getAttendanceListByMonth(
            @Parameter(description = "司机ID") @PathVariable Long driverId,
            @Parameter(description = "年月(yyyy-MM)") @RequestParam String yearMonth) {
        List<AttendanceVO> list = attendanceService.getAttendanceListByMonth(driverId, yearMonth);
        return Result.success(list);
    }

    @Operation(summary = "获取某月考勤统计")
    @GetMapping("/{driverId}/statistics")
    public Result<AttendanceStatisticsVO> getAttendanceStatistics(
            @Parameter(description = "司机ID") @PathVariable Long driverId,
            @Parameter(description = "年月(yyyy-MM)") @RequestParam String yearMonth) {
        AttendanceStatisticsVO statistics = attendanceService.getAttendanceStatistics(driverId, yearMonth);
        return Result.success(statistics);
    }

    @Operation(summary = "补卡（管理员功能）")
    @PutMapping("/{attendanceId}/supplement")
    public Result<AttendanceVO> supplementAttendance(
            @Parameter(description = "出勤记录ID") @PathVariable Long attendanceId,
            @Parameter(description = "上班时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInTime,
            @Parameter(description = "下班时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutTime,
            @Parameter(description = "状态(1-正常,2-迟到,3-早退,4-缺勤)") @RequestParam(required = false) Integer status,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        AttendanceVO vo = attendanceService.supplementAttendance(attendanceId, checkInTime, checkOutTime, status, remark);
        return Result.success(vo);
    }
}
