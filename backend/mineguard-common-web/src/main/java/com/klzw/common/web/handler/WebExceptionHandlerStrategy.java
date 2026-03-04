package com.klzw.common.web.handler;

import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import com.klzw.common.web.exception.WebException;
import org.springframework.stereotype.Component;

@Component
public class WebExceptionHandlerStrategy implements ExceptionHandlerStrategy {
    @Override
    public boolean support(Throwable exception) {
        return exception instanceof WebException;
    }

    @Override
    public Result<?> handle(Throwable exception) {
        WebException webException = (WebException) exception;
        return Result.fail(webException.getCode(), webException.getMessage());
    }
}
