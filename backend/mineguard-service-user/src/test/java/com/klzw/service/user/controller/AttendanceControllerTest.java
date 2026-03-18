package com.klzw.service.user.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.user.dto.CheckInDTO;
import com.klzw.service.user.dto.CheckOutDTO;
import com.klzw.service.user.service.AttendanceService;
import com.klzw.service.user.vo.AttendanceStatisticsVO;
import com.klzw.service.user.vo.AttendanceVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AttendanceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private AttendanceController attendanceController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(attendanceController).build();
    }

    @Test
    void testCheckIn() throws Exception {
        CheckInDTO dto = new CheckInDTO();
        dto.setDriverId(123L);
        dto.setRemark("上班打卡");

        AttendanceVO expectedVO = new AttendanceVO();
        expectedVO.setId("1");
        expectedVO.setDriverId("123");

        when(attendanceService.checkIn(any(CheckInDTO.class))).thenReturn(expectedVO);

        mockMvc.perform(post("/attendance/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"driverId\":123,\"remark\":\"上班打卡\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(attendanceService, times(1)).checkIn(any(CheckInDTO.class));
    }

    @Test
    void testCheckOut() throws Exception {
        CheckOutDTO dto = new CheckOutDTO();
        dto.setDriverId(123L);
        dto.setRemark("下班打卡");

        AttendanceVO expectedVO = new AttendanceVO();
        expectedVO.setId("1");
        expectedVO.setDriverId("123");

        when(attendanceService.checkOut(any(CheckOutDTO.class))).thenReturn(expectedVO);

        mockMvc.perform(post("/attendance/check-out")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"driverId\":123,\"remark\":\"下班打卡\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(attendanceService, times(1)).checkOut(any(CheckOutDTO.class));
    }

    @Test
    void testGetAttendanceByDate() throws Exception {
        Long driverId = 123L;
        LocalDate date = LocalDate.now();

        AttendanceVO expectedVO = new AttendanceVO();
        expectedVO.setId("1");
        expectedVO.setDriverId(String.valueOf(driverId));

        when(attendanceService.getAttendanceByDate(driverId, date)).thenReturn(expectedVO);

        mockMvc.perform(get("/attendance/{driverId}", driverId)
                .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(attendanceService, times(1)).getAttendanceByDate(driverId, date);
    }

    @Test
    void testGetAttendanceListByMonth() throws Exception {
        Long driverId = 123L;
        String yearMonth = "2026-03";

        AttendanceVO attendanceVO = new AttendanceVO();
        attendanceVO.setId("1");
        attendanceVO.setDriverId(String.valueOf(driverId));

        when(attendanceService.getAttendanceListByMonth(driverId, yearMonth))
                .thenReturn(Collections.singletonList(attendanceVO));

        mockMvc.perform(get("/attendance/{driverId}/list", driverId)
                .param("yearMonth", yearMonth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(attendanceService, times(1)).getAttendanceListByMonth(driverId, yearMonth);
    }

    @Test
    void testGetAttendanceStatistics() throws Exception {
        Long driverId = 123L;
        String yearMonth = "2026-03";

        AttendanceStatisticsVO expectedVO = new AttendanceStatisticsVO();
        expectedVO.setShouldAttendanceDays(22);
        expectedVO.setActualAttendanceDays(20);
        expectedVO.setNormalDays(18);
        expectedVO.setLateTimes(1);
        expectedVO.setEarlyLeaveTimes(1);
        expectedVO.setAbsentDays(0);

        when(attendanceService.getAttendanceStatistics(driverId, yearMonth)).thenReturn(expectedVO);

        mockMvc.perform(get("/attendance/{driverId}/statistics", driverId)
                .param("yearMonth", yearMonth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(attendanceService, times(1)).getAttendanceStatistics(driverId, yearMonth);
    }

    @Test
    void testSupplementAttendance() throws Exception {
        Long attendanceId = 1L;
        LocalDate checkInTime = LocalDate.now();
        LocalDate checkOutTime = LocalDate.now();
        Integer status = 1;
        String remark = "补卡";

        AttendanceVO expectedVO = new AttendanceVO();
        expectedVO.setId(String.valueOf(attendanceId));
        expectedVO.setStatus(status);

        when(attendanceService.supplementAttendance(attendanceId, checkInTime, checkOutTime, status, remark))
                .thenReturn(expectedVO);

        mockMvc.perform(put("/attendance/{attendanceId}/supplement", attendanceId)
                .param("checkInTime", checkInTime.toString())
                .param("checkOutTime", checkOutTime.toString())
                .param("status", status.toString())
                .param("remark", remark))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(attendanceService, times(1))
                .supplementAttendance(attendanceId, checkInTime, checkOutTime, status, remark);
    }
}
