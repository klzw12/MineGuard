package com.klzw.common.core.exception;

import com.klzw.common.core.enums.ResultCodeEnum;
import com.klzw.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
public class MaxUploadSizeExceededExceptionHandlerStrategy implements ExceptionHandlerStrategy {
    @Override
    public boolean support(Throwable throwable) {
        return throwable instanceof MaxUploadSizeExceededException;
    }

    @Override
    public Result<?> handle(Throwable throwable) {
        MaxUploadSizeExceededException ex = (MaxUploadSizeExceededException) throwable;
        log.error("文件上传大小超限: {}", ex.getMessage());
        return Result.fail(ResultCodeEnum.PARAM_ERROR.getCode(), "文件大小超过限制，请压缩文件或降低画质后重试");
    }
}