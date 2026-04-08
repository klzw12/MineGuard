package com.klzw.common.mq.constant;

/**
 * 消息队列错误码枚举
 * 错误码范围：1200-1299
 */
public enum MqResultCode {
    
    MQ_ERROR(1200, "消息队列操作失败"),
    MQ_CONNECTION_ERROR(1201, "消息队列连接失败"),
    MQ_TIMEOUT_ERROR(1202, "消息队列操作超时"),
    
    PRODUCER_SEND_ERROR(1210, "消息发送失败"),
    PRODUCER_TRANSACTION_ERROR(1211, "事务消息处理失败"),
    
    CONSUMER_PROCESS_ERROR(1220, "消息消费失败"),
    CONSUMER_RETRY_EXCEEDED(1221, "消息重试次数超过限制"),
    
    QUEUE_NOT_FOUND(1230, "队列不存在"),
    QUEUE_FULL(1231, "队列已满"),
    
    EXCHANGE_NOT_FOUND(1240, "交换机不存在"),
    EXCHANGE_TYPE_ERROR(1241, "交换机类型错误"),
    
    MESSAGE_FORMAT_ERROR(1250, "消息格式错误"),
    MESSAGE_TOO_LARGE(1251, "消息过大");
    
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
