package com.klzw.service.dispatch.scheduler;

import com.klzw.service.dispatch.entity.TransportTask;
import com.klzw.service.dispatch.service.DispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchScheduler {

    private final DispatchService dispatchService;

    @Scheduled(cron = "0 0 * * * ?")
    public void checkAndExecuteDispatchTasks() {
        try {
            log.info("定时检查并执行调度任务");
            
            // 获取待执行的调度任务
            List<TransportTask> pendingTasks = dispatchService.getDispatchTaskList(0, null, null);
            
            if (pendingTasks != null && !pendingTasks.isEmpty()) {
                log.info("发现待执行任务数量：{}", pendingTasks.size());
                
                int successCount = 0;
                int failCount = 0;
                
                for (TransportTask task : pendingTasks) {
                    try {
                        boolean success = dispatchService.executeDispatch(task.getId());
                        if (success) {
                            successCount++;
                            log.info("执行调度任务成功：任务ID={}, 任务编号={}", task.getId(), task.getTaskNo());
                        } else {
                            failCount++;
                            log.warn("执行调度任务失败：任务ID={}, 任务编号={}", task.getId(), task.getTaskNo());
                        }
                    } catch (Exception e) {
                        failCount++;
                        log.error("执行调度任务异常：任务ID={}", task.getId(), e);
                    }
                }
                
                log.info("定时调度任务执行完成：成功={}, 失败={}", successCount, failCount);
            } else {
                log.info("没有待执行的调度任务");
            }
            
        } catch (Exception e) {
            log.error("定时检查并执行调度任务异常", e);
        }
    }
}
