package com.klzw.service.warning.scheduler;

import com.klzw.service.warning.manager.TripWarningManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarningScheduler {

    private final TripWarningManager tripWarningManager;

    /**
     * 定时检查活跃行程的预警状态
     * 每30秒执行一次
     */
    @Scheduled(fixedRate = 30000)
    public void checkActiveTrips() {
        try {
            // 获取活跃行程列表
            Set<Long> activeTrips = tripWarningManager.getActiveTrips();
            log.info("当前活跃行程数量：{}", activeTrips.size());
        } catch (Exception e) {
            log.error("检查活跃行程异常", e);
        }
    }
}

