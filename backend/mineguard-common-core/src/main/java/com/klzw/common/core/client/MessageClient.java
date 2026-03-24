package com.klzw.common.core.client;

import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 消息服务客户端
 * 用于调度模块调用 user 模块的消息推送功能
 */
@HttpExchange
public interface MessageClient {

    /**
     * 发送调度任务通知
     * 
     * @param userId 用户 ID
     * @param title 消息标题
     * @param content 消息内容
     * @param type 消息类型
     * @param businessId 业务 ID
     */
    @PostExchange("/api/message/send")
    void sendMessage(
        Long userId,
        String title,
        String content,
        String type,
        String businessId
    );

    /**
     * 按角色发送消息
     * 
     * @param role 角色
     * @param title 消息标题
     * @param content 消息内容
     * @param type 消息类型
     */
    @PostExchange("/api/message/send-by-role")
    void sendMessageByRole(
        String role,
        String title,
        String content,
        String type
    );
}
