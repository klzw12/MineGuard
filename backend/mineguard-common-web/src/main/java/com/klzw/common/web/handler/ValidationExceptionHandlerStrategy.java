package com.klzw.common.web.handler;

import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.stream.Collectors;

@Slf4j
@Component
public class ValidationExceptionHandlerStrategy implements ExceptionHandlerStrategy {

    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.error("参数校验失败: {}", message);
        return Result.fail(601, message);
    }

    public Result<?> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.error("参数绑定失败: {}", message);
        return Result.fail(601, message);
    }

    @Override
    public boolean support(Throwable exception) {
        return exception instanceof MethodArgumentNotValidException || exception instanceof BindException;
    }

    @Override
    public Result<?> handle(Throwable exception) {
        if (exception instanceof MethodArgumentNotValidException) {
            return handleMethodArgumentNotValidException((MethodArgumentNotValidException) exception);
        } else if (exception instanceof BindException) {
            return handleBindException((BindException) exception);
        }
        return null;
    }
}