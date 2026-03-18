package com.klzw.service.user.handler;

import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import com.klzw.service.user.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户模块异常处理策略
 * <p>
 * 用于处理UserException异常，将异常转换为统一的Result响应
 * <p>
 * 功能：
 * 1. 识别UserException异常
 * 2. 记录异常日志
 * 3. 返回标准化的错误响应
 */
@Slf4j
@Component
public class UserExceptionHandlerStrategy implements ExceptionHandlerStrategy {

    @Override
    public boolean support(Throwable throwable) {
        return throwable instanceof UserException;
    }

    @Override
    public Result<?> handle(Throwable throwable) {
        UserException exception = (UserException) throwable;
        
        log.error("用户模块异常: code={}, message={}", 
                exception.getCode(), 
                exception.getMessage());
        
        return Result.fail(exception.getCode(), exception.getMessage());
    }
}
