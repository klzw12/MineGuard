package com.klzw.common.core.exception;

import com.klzw.common.core.enums.ResultCodeEnum;
import com.klzw.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultExceptionHandlerStrategy implements ExceptionHandlerStrategy {
    @Override
    public boolean support(Throwable throwable) {
        return true;
    }

    @Override
    public Result<?> handle(Throwable throwable) {
        log.error("未处理异常: message={}", throwable.getMessage(), throwable);
        return Result.fail(ResultCodeEnum.INTERNAL_ERROR.getCode(), "系统内部错误: " + throwable.getMessage());
    }
}
