package com.klzw.service.dispatch.controller;

import com.klzw.service.dispatch.entity.TransportTask;
import com.klzw.service.dispatch.service.DispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    @PostMapping("/task")
    public TransportTask createDispatchTask(@RequestBody TransportTask task) {
        try {
            log.debug("创建调度任务：{}", task);
            return dispatchService.createDispatchTask(task);
        } catch (Exception e) {
            log.error("创建调度任务异常", e);
            throw e;
        }
    }

    @PutMapping("/task")
    public TransportTask updateDispatchTask(@RequestBody TransportTask task) {
        try {
            log.debug("更新调度任务：{}", task);
            return dispatchService.updateDispatchTask(task);
        } catch (Exception e) {
            log.error("更新调度任务异常", e);
            throw e;
        }
    }

    @PostMapping("/task/{taskId}/execute")
    public boolean executeDispatch(@PathVariable Long taskId) {
        try {
            log.debug("执行调度任务：ID={}", taskId);
            return dispatchService.executeDispatch(taskId);
        } catch (Exception e) {
            log.error("执行调度任务异常", e);
            throw e;
        }
    }

    @PostMapping("/task/{taskId}/cancel")
    public boolean cancelDispatchTask(@PathVariable Long taskId) {
        try {
            log.debug("取消调度任务：ID={}", taskId);
            return dispatchService.cancelDispatchTask(taskId);
        } catch (Exception e) {
            log.error("取消调度任务异常", e);
            throw e;
        }
    }

    @GetMapping("/task/{taskId}")
    public TransportTask getDispatchTask(@PathVariable Long taskId) {
        try {
            log.debug("获取调度任务：ID={}", taskId);
            return dispatchService.getDispatchTask(taskId);
        } catch (Exception e) {
            log.error("获取调度任务异常", e);
            throw e;
        }
    }

    @GetMapping("/task/list")
    public List<TransportTask> getDispatchTaskList(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            log.debug("获取调度任务列表：状态={}, 开始日期={}, 结束日期={}", 
                    status, startDate, endDate);
            return dispatchService.getDispatchTaskList(status, start, end);
        } catch (Exception e) {
            log.error("获取调度任务列表异常", e);
            throw e;
        }
    }
    
    @PostMapping("/task/dynamic-adjust/user-leave")
    public void reassignTasksByUserLeave(
            @RequestParam Long userId,
            @RequestParam String roleCode) {
        try {
            log.info("用户请假任务重新分配：用户 ID={}, 角色编码={}", userId, roleCode);
            dispatchService.reassignTasksByUserLeave(userId, roleCode);
        } catch (Exception e) {
            log.error("用户请假任务重新分配异常", e);
            throw e;
        }
    }
}
