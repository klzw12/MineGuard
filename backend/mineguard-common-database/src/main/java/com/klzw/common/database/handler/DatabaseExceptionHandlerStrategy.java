package com.klzw.common.database.handler;

import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import com.klzw.common.database.exception.DatabaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 数据库异常处理策略
 * <p>
 * 用于处理DatabaseException异常，将异常转换为统一的Result响应
 * <p>
 * 功能：
 * 1. 识别DatabaseException异常
 * 2. 记录异常日志
 * 3. 返回标准化的错误响应
 */
@Slf4j
@Component
public class DatabaseExceptionHandlerStrategy implements ExceptionHandlerStrategy {

    @Override
    public boolean support(Throwable throwable) {
        return throwable instanceof DatabaseException;
    }

    @Override
    public Result<?> handle(Throwable throwable) {
        DatabaseException exception = (DatabaseException) throwable;
        
        log.error("数据库异常: code={}, message={}, module={}", 
                exception.getCode(), 
                exception.getMessage(), 
                exception.getModule(), 
                exception);
        
        return Result.fail(exception.getCode(), exception.getMessage());
    }
}
