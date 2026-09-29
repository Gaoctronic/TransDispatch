package com.example.vehicleapi.core.error;

import com.example.vehicleapi.core.web.ApiResponse;
import com.example.vehicleapi.core.web.ErrorCodes;
import com.example.vehicleapi.core.web.FieldError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.validation.method.ParameterValidationResult;

/**
 * 全局异常处理器：保证任何错误都返回统一响应格式。
 *
 * <p>对应 Python 版本的 {@code app/core/errors.py}。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger("vehicle_api");

    /** 业务异常（404 车辆不存在 / 409 版本冲突 / 422 参数校验失败）。 */
    @ExceptionHandler(VehicleApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleVehicleApiError(VehicleApiException exception) {
        return ResponseEntity.status(exception.getHttpStatus()).body(exception.toPayload());
    }

    /** 请求体校验失败（Bean Validation）。 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception) {
        List<FieldError> errors =
                exception.getBindingResult().getFieldErrors().stream()
                        .map(ValidationErrorMapper::toFieldError)
                        .toList();
        if (errors.isEmpty()) {
            errors =
                    exception.getBindingResult().getGlobalErrors().stream()
                            .map(
                                    error ->
                                            new FieldError(
                                                    "body",
                                                    "value_error",
                                                    ValidationErrorMapper.messageOf(error)))
                            .toList();
        }
        return validationResponse(errors);
    }

    /** 方法参数校验失败（例如 @PathVariable / @RequestParam 上的约束）。 */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleHandlerMethodValidation(
            HandlerMethodValidationException exception) {
        List<FieldError> errors = new ArrayList<>();
        for (ParameterValidationResult result : exception.getAllValidationResults()) {
            MethodParameter parameter = result.getMethodParameter();
            String name = parameter.getParameterName();
            String field = (name == null || name.isBlank()) ? "path" : "path." + name;
            for (MessageSourceResolvable error : result.getResolvableErrors()) {
                errors.add(new FieldError(field, "value_error", ValidationErrorMapper.messageOf(error)));
            }
        }
        return validationResponse(errors);
    }

    /** 方法级约束违例（@Validated 生效时的兜底）。 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
            ConstraintViolationException exception) {
        List<FieldError> errors =
                exception.getConstraintViolations().stream()
                        .map(
                                violation ->
                                        new FieldError(
                                                parameterField(violation.getPropertyPath().toString()),
                                                "value_error",
                                                violation.getMessage()))
                        .toList();
        return validationResponse(errors);
    }

    /** 请求体无法解析：JSON 语法错误、未知字段、枚举/类型不匹配等。 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleMessageNotReadable(
            HttpMessageNotReadableException exception) {
        return validationResponse(List.of(JsonErrorMapper.toFieldError(exception)));
    }

    /** 路由不存在（静态资源也会走到这里）。 */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ApiResponse<Object>> handleNotFound(Exception exception) {
        return frameworkError(404, ErrorCodes.MESSAGE_NOT_FOUND);
    }

    /** 请求方法不支持。 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception) {
        return frameworkError(405, ErrorCodes.MESSAGE_METHOD_NOT_ALLOWED);
    }

    /** 其他框架级错误（保留其 HTTP 状态码，业务 code 与 HTTP 状态码一致）。 */
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiResponse<Object>> handleErrorResponse(ErrorResponseException exception) {
        return frameworkError(exception.getStatusCode().value(), detailOf(exception));
    }

    /** 媒体类型不支持 / 不可接受。 */
    @ExceptionHandler({
        HttpMediaTypeNotSupportedException.class,
        HttpMediaTypeNotAcceptableException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleMediaTypeError(Exception exception) {
        ErrorResponse errorResponse = (ErrorResponse) exception;
        return frameworkError(errorResponse.getStatusCode().value(), detailOf(errorResponse));
    }

    /** 未捕获异常。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(
            Exception exception, HttpServletRequest request) {
        log.error("未处理的服务端异常: {} {}", request.getMethod(), request.getRequestURI(), exception);
        return frameworkError(500, ErrorCodes.MESSAGE_INTERNAL_ERROR);
    }

    private static String parameterField(String propertyPath) {
        int index = propertyPath.lastIndexOf('.');
        String name = index < 0 ? propertyPath : propertyPath.substring(index + 1);
        return name.isEmpty() ? "path" : "path." + name;
    }

    private static String detailOf(ErrorResponse errorResponse) {
        if (errorResponse.getBody() != null && errorResponse.getBody().getDetail() != null) {
            return errorResponse.getBody().getDetail();
        }
        return "HTTP 请求错误";
    }

    private static ResponseEntity<ApiResponse<Object>> validationResponse(List<FieldError> errors) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("errors", errors);
        return ResponseEntity.status(422)
                .body(
                        ApiResponse.<Object>error(
                                ErrorCodes.VALIDATION_ERROR,
                                ErrorCodes.MESSAGE_VALIDATION_ERROR,
                                data));
    }

    private static ResponseEntity<ApiResponse<Object>> frameworkError(int status, String message) {
        return ResponseEntity.status(status)
                .body(ApiResponse.<Object>error(status, message, null));
    }
}
