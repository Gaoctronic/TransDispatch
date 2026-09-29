package com.example.vehicleapi.core.error;

import com.example.vehicleapi.core.web.ApiResponse;

/**
 * 业务异常基类：由全局异常处理器转换成统一响应格式。
 *
 * <p>对应 Python 版本的 {@code app/core/exceptions.py}。
 */
public class VehicleApiException extends RuntimeException {

    private final int httpStatus;
    private final int code;
    private final transient Object data;

    public VehicleApiException(int httpStatus, int code, String message, Object data) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.data = data;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public int getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }

    /** 转换成统一响应体。 */
    public ApiResponse<Object> toPayload() {
        return ApiResponse.error(code, getMessage(), data);
    }
}
