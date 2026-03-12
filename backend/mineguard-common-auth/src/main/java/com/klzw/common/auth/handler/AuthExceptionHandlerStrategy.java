package com.klzw.common.auth.handler;

import com.klzw.common.auth.exception.AuthException;
import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 认证异常处理策略
 * <p>
 * 用于处理AuthException异常，将异常转换为统一的Result响应
 * <p>
 * 功能：
 * 1. 识别AuthException异常
 * 2. 记录异常日志
 * 3. 返回标准化的错误响应
 */
@Slf4j
@Component
public class AuthExceptionHandlerStrategy implements ExceptionHandlerStrategy {

    @Override
    public boolean support(Throwable throwable) {
        return throwable instanceof AuthException;
    }

    @Override
    public Result<?> handle(Throwable throwable) {
        AuthException exception = (AuthException) throwable;
        
        log.error("认证异常: code={}, message={}, module={}", 
                exception.getCode(), 
                exception.getMessage(), 
                exception.getModule());
        
        return Result.fail(exception.getCode(), exception.getMessage());
    }
}
