package com.klzw.common.core.exception;

import com.klzw.common.core.result.Result;

/**
 * 异常处理策略接口
 */
public interface ExceptionHandlerStrategy {
    boolean support(Throwable throwable);
    Result<?> handle(Throwable throwable);
}
