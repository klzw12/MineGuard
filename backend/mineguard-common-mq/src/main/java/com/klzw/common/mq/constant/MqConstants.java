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

}
