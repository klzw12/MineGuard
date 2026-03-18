package com.klzw.service.trip.exception;

import com.klzw.common.core.exception.BaseException;
import com.klzw.service.trip.constant.TripResultCode;
import lombok.Getter;

@Getter
public class TripException extends BaseException {

    private static final String MODULE = "trip";

    public TripException(int code, String message) {
        super(code, message, MODULE);
    }

    public TripException(TripResultCode resultCode) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE);
    }

    public TripException(TripResultCode resultCode, String message) {
        super(resultCode.getCode(), message, MODULE);
    }

    public TripException(TripResultCode resultCode, Throwable cause) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE, cause);
    }
}
