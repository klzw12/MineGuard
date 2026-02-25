package com.klzw.common.core.exception;

import com.klzw.common.core.result.Result;

public interface ExceptionHandlerStrategy {
    boolean support(Throwable throwable);
    Result<?> handle(Throwable throwable);
}
