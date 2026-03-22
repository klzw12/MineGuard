package com.klzw.common.core.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange
public interface DispatchClient {

    @GetExchange("/api/dispatch/task/driver/{driverId}/pending")
    List<Long> getPendingTaskIdsByDriverId(@PathVariable("driverId") Long driverId);

    @PostExchange("/api/dispatch/task/dynamic-adjust/user-leave")
    void reassignTasksByUserLeave(
        @RequestParam("userId") Long userId,
        @RequestParam("roleCode") String roleCode);
}
