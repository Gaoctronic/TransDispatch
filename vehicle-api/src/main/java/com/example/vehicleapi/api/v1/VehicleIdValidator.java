package com.example.vehicleapi.api.v1;

import com.example.vehicleapi.core.error.RequestValidationException;
import com.example.vehicleapi.core.web.FieldError;
import java.util.regex.Pattern;

/**
 * 车辆 ID 的取值约束。
 *
 * <p>与 Python 版本保持一致：长度 1~64，且只允许 {@code [A-Za-z0-9._:-]}。
 * 这里手工校验而不是写在 {@code @PathVariable} 的路径正则里，是为了让非法 ID 返回 422 而不是 404。
 */
public final class VehicleIdValidator {

    public static final int MAX_LENGTH = 64;

    private static final Pattern PATTERN = Pattern.compile("^[A-Za-z0-9._:-]+$");
    private static final String PATTERN_DESCRIPTION = "^[A-Za-z0-9._:-]+$";

    private VehicleIdValidator() {}

    public static void validate(String vehicleId) {
        if (vehicleId == null || vehicleId.isEmpty()) {
            throw new RequestValidationException(
                    new FieldError(
                            "path.vehicle_id",
                            "string_too_short",
                            "String should have at least 1 character"));
        }
        if (vehicleId.length() > MAX_LENGTH) {
            throw new RequestValidationException(
                    new FieldError(
                            "path.vehicle_id",
                            "string_too_long",
                            "String should have at most " + MAX_LENGTH + " characters"));
        }
        if (!PATTERN.matcher(vehicleId).matches()) {
            throw new RequestValidationException(
                    new FieldError(
                            "path.vehicle_id",
                            "string_pattern_mismatch",
                            "String should match pattern '" + PATTERN_DESCRIPTION + "'"));
        }
    }
}
