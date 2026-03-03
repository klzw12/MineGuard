package com.klzw.common.mq.constant;

/**
 * 消息队列错误码枚举
 * 错误码范围：1100-1199
 */
public enum MqResultCode {
    
    // 通用错误 1100-1109
    MQ_ERROR(1100, "消息队列操作失败"),
    MQ_CONNECTION_ERROR(1101, "消息队列连接失败"),
    MQ_TIMEOUT_ERROR(1102, "消息队列操作超时"),
    
    // 生产者错误 1110-1119
    PRODUCER_SEND_ERROR(1110, "消息发送失败"),
    PRODUCER_TRANSACTION_ERROR(1111, "事务消息处理失败"),
    
    // 消费者错误 1120-1129
    CONSUMER_PROCESS_ERROR(1120, "消息消费失败"),
    CONSUMER_RETRY_EXCEEDED(1121, "消息重试次数超过限制"),
    
    // 队列错误 1130-1139
    QUEUE_NOT_FOUND(1130, "队列不存在"),
    QUEUE_FULL(1131, "队列已满"),
    
    // 交换机错误 1140-1149
    EXCHANGE_NOT_FOUND(1140, "交换机不存在"),
    EXCHANGE_TYPE_ERROR(1141, "交换机类型错误"),
    
    // 其他错误 1150-1199
    MESSAGE_FORMAT_ERROR(1150, "消息格式错误"),
    MESSAGE_TOO_LARGE(1151, "消息过大"),
    ;
    
    private final int code;
    private final String message;
    
    MqResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
