package com.example.vehicleapi.domain.vehicle;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 校验 doors / windows / tirePressure 的键是否合法。
 *
 * <p>对应 Python 版本 {@code VehicleStatusPatch} 上两个 {@code field_validator} 的逻辑。
 */
@Documented
@Constraint(validatedBy = AllowedStatusKeysValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedStatusKeys {

    String message() default "车辆状态包含不支持的键";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
