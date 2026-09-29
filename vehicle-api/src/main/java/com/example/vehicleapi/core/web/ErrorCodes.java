package com.example.vehicleapi.core.web;

/**
 * 业务状态码（与 HTTP 状态码解耦，统一放在响应体 code 字段里）。
 *
 * <p>对应 Python 版本的 {@code app/core/response.py}。
 */
public final class ErrorCodes {

    public static final int SUCCESS = 0;
    public static final int VALIDATION_ERROR = 40001;
    public static final int NOT_FOUND = 40401;
    public static final int VERSION_CONFLICT = 40901;
    public static final int INTERNAL_ERROR = 50000;

    public static final String MESSAGE_SUCCESS = "success";
    public static final String MESSAGE_VALIDATION_ERROR = "请求参数校验失败";
    public static final String MESSAGE_NOT_FOUND = "Not Found";
    public static final String MESSAGE_METHOD_NOT_ALLOWED = "Method Not Allowed";
    public static final String MESSAGE_INTERNAL_ERROR = "服务器内部错误";

    private ErrorCodes() {}
}
