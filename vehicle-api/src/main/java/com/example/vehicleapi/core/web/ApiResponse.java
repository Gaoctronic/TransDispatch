package com.example.vehicleapi.core.web;

/**
 * 统一响应外壳。
 *
 * <p>所有接口（包括错误响应）都返回如下结构：
 *
 * <pre>{"code": 0, "message": "success", "data": ...}</pre>
 *
 * <p>对应 Python 版本的 {@code app/core/response.py}。
 *
 * <p>注意：序列化时保留 null 字段（不启用 NON_NULL），与 Pydantic 的默认行为保持一致，
 * 例如未上报的状态字段会以 {@code "charging": null} 的形式出现。
 */
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    public ApiResponse() {}

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 构造成功响应。 */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ErrorCodes.SUCCESS, ErrorCodes.MESSAGE_SUCCESS, data);
    }

    /** 构造失败响应（供异常处理器使用）。 */
    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
