package com.klzw.common.core.client;

import com.klzw.common.core.result.Result;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@HttpExchange
public interface UserClient {

    @GetExchange("/user/exists/{id}")
    Result<Boolean> existsUser(@PathVariable("id") Long id);

    @GetExchange("/user/{id}")
    Result<Object> getUserById(@PathVariable("id") Long id);

    @GetExchange("/user/leave-ids")
    Result<List<Long>> getLeaveUserIds();

    @GetExchange("/attendance/statistics/internal")
    Result<Map<String, Object>> getAttendanceStatistics(
            @RequestParam("userId") Long userId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

    @GetExchange("/user/{userId}/role")
    Result<String> getUserRole(@PathVariable("userId") Long userId);
}
