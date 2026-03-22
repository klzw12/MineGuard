package com.klzw.common.core.client;

import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 任务接单客户端
 * 用于调度模块调用 user 模块的任务接单功能
 */
@HttpExchange
public interface TaskClient {

    /**
     * 用户接单（支持司机、维修员、安全员）
     * 
     * @param userId 用户 ID
     * @param taskId 任务 ID
     * @param taskType 任务类型：transport-运输任务，maintenance-维修任务，inspection-巡检任务
     * @return 接单结果：true-成功，false-失败
     */
    @PostExchange("/api/task/accept")
    Boolean acceptTask(
        Long userId,
        Long taskId,
        String taskType
    );
}
