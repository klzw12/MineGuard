package com.klzw.service.dispatch.scheduler;

import com.klzw.service.dispatch.service.DispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchScheduler {

    private final DispatchService dispatchService;

    @Scheduled(cron = "0 0 * * * ?")
    public void checkAndExecuteDispatchTasks() {
        try {
            log.info("定时检查并执行调度任务");
            log.info("定时检查并执行调度任务完成");
        } catch (Exception e) {
            log.error("定时检查并执行调度任务异常", e);
        }
    }
}
