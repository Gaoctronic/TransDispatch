package com.example.vehicleapi.core.error;

import com.example.vehicleapi.core.web.ErrorCodes;
import com.example.vehicleapi.core.web.FieldError;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 参数校验失败（HTTP 422 / code 40001）。
 *
 * <p>用于 Bean Validation 覆盖不到的校验（例如路径参数 {@code path.vehicle_id}）。
 */
public class RequestValidationException extends VehicleApiException {

    public RequestValidationException(FieldError fieldError) {
        this(List.of(fieldError));
    }

    public RequestValidationException(List<FieldError> errors) {
        super(
                422,
                ErrorCodes.VALIDATION_ERROR,
                ErrorCodes.MESSAGE_VALIDATION_ERROR,
                details(errors));
    }

    private static Map<String, Object> details(List<FieldError> errors) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("errors", errors);
        return data;
    }
}
