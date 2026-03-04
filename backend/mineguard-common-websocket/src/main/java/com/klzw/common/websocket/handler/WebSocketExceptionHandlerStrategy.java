package com.klzw.common.websocket.handler;

import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import com.klzw.common.websocket.exception.WebSocketException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketExceptionHandlerStrategy implements ExceptionHandlerStrategy {
    @Override
    public boolean support(Throwable throwable) {
        return throwable instanceof WebSocketException;
    }

    @Override
    public Result<?> handle(Throwable throwable) {
        WebSocketException exception = (WebSocketException) throwable;
        log.error("WebSocket异常: code={}, message={}, module={}", 
                exception.getCode(), 
                exception.getMessage(), 
                exception.getModule(), 
                exception);
        return Result.fail(exception.getCode(), exception.getMessage());
    }
}
