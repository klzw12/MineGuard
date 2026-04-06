package com.klzw.common.mq.constant;

/**
 * 消息队列常量类
 */
public class MqConstants {

    /**
     * 交换机名称
     */
    public static final String DEFAULT_EXCHANGE = "default.exchange";
    public static final String DELAY_EXCHANGE = "delay.exchange";
    public static final String DEAD_LETTER_EXCHANGE = "dead.letter.exchange";

    /**
     * 队列名称
     */
    public static final String DEFAULT_QUEUE = "default.queue";
    public static final String DELAY_QUEUE = "delay.queue";
    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";

    /**
     * 路由键
     */
    public static final String DEFAULT_ROUTING_KEY = "default.routing.key";
    public static final String DELAY_ROUTING_KEY = "delay.routing.key";

    /**
     * 离线消息交换机
     */
    public static final String OFFLINE_MESSAGE_EXCHANGE = "offline.message.exchange";

    /**
     * 离线消息队列前缀
     */
    public static final String OFFLINE_MESSAGE_QUEUE_PREFIX = "offline.message.";

    /**
     * 离线消息路由键前缀
     */
    public static final String OFFLINE_MESSAGE_ROUTING_KEY_PREFIX = "offline.";

    /**
     * 调度任务交换机
     */
    public static final String DISPATCH_TASK_EXCHANGE = "dispatch.task.exchange";

    /**
     * 调度任务队列
     */
    public static final String DISPATCH_TASK_QUEUE = "dispatch.task.queue";

    /**
     * 调度任务路由键
     */
    public static final String DISPATCH_TASK_ROUTING_KEY = "dispatch.task.routing";

}
