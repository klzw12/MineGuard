package com.klzw.common.mongodb.handler;

import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.mongodb.exception.MongoDbException;
import com.klzw.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MongoDB异常处理策略
 * <p>
 * 用于处理MongoDB模块的异常，返回标准化的错误响应
 */
@Slf4j
@Component
public class MongoDbExceptionHandlerStrategy implements ExceptionHandlerStrategy {

    /**
     * 检查是否支持处理指定的异常
     *
     * @param throwable 异常对象
     * @return 是否支持处理
     */
    @Override
    public boolean support(Throwable throwable) {
        return throwable instanceof MongoDbException;
    }

    /**
     * 处理异常，返回标准化的错误响应
     *
     * @param throwable 异常对象
     * @return 标准化的错误响应
     */
    @Override
    public Result<?> handle(Throwable throwable) {
        MongoDbException exception = (MongoDbException) throwable;
        
        log.error("MongoDB异常: code={}, message={}", 
                exception.getCode(), 
                exception.getMessage(), 
                exception);
        
        return Result.fail(exception.getCode(), exception.getMessage());
    }
}