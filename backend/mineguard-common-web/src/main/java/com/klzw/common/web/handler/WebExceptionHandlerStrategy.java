package com.klzw.common.web.handler;

import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import com.klzw.common.web.exception.WebException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebExceptionHandlerStrategy implements ExceptionHandlerStrategy {
    @Override
    public boolean support(Throwable exception) {
        return exception instanceof WebException;
    }

    @Override
    public Result<?> handle(Throwable exception) {
        WebException webException = (WebException) exception;
        if (webException.getCause() != null) {
            log.error("Web异常: code={}, message={}, caused by: {}", webException.getCode(), webException.getMessage(), webException.getCause().getMessage(), webException.getCause());
        } else {
            log.error("Web异常: code={}, message={}", webException.getCode(), webException.getMessage());
        }
        return Result.fail(webException.getCode(), webException.getMessage());
    }
}
