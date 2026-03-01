package com.klzw.common.mq.exception;

import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import org.springframework.stereotype.Component;

/**
 * 消息队列异常处理策略
 */
@Component
public class MqExceptionHandlerStrategy implements ExceptionHandlerStrategy {
    
    @Override
    public boolean support(Throwable throwable) {
        return throwable instanceof MqException;
    }
    
    @Override
    public Result<?> handle(Throwable throwable) {
        MqException ex = (MqException) throwable;
        return Result.fail(ex.getCode(), ex.getMessage());
    }
}
