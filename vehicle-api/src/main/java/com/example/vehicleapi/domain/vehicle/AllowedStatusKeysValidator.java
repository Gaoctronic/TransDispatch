package com.example.vehicleapi.domain.vehicle;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** {@link AllowedStatusKeys} 的具体校验逻辑。 */
public class AllowedStatusKeysValidator
        implements ConstraintValidator<AllowedStatusKeys, VehicleStatusPatch> {

    @Override
    public boolean isValid(VehicleStatusPatch value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        List<Problem> problems = new ArrayList<>();
        collectUnknownKeys(problems, "doors", value.getDoors(), VehicleKeys.DOOR_KEYS);
        collectUnknownKeys(problems, "windows", value.getWindows(), VehicleKeys.WINDOW_KEYS);
        collectUnknownKeys(problems, "tirePressure", value.getTirePressure(), VehicleKeys.TIRE_KEYS);
        collectTirePressureRange(problems, value.getTirePressure());

        if (problems.isEmpty()) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        for (Problem problem : problems) {
            context
                    .buildConstraintViolationWithTemplate(problem.message())
                    .addPropertyNode(problem.property())
                    .addConstraintViolation();
        }
        return false;
    }

    private static void collectUnknownKeys(
            List<Problem> problems, String property, Map<String, ?> value, Set<String> allowed) {
        if (value == null) {
            return;
        }
        Set<String> unknown = new TreeSet<>();
        for (String key : value.keySet()) {
            if (!allowed.contains(key)) {
                unknown.add(key);
            }
        }
        if (!unknown.isEmpty()) {
            problems.add(
                    new Problem(
                            property,
                            property
                                    + " 含有不支持的键 "
                                    + unknown
                                    + "，允许的键为 "
                                    + new TreeSet<>(allowed)));
        }
    }

    private static void collectTirePressureRange(
            List<Problem> problems, Map<String, Double> value) {
        if (value == null) {
            return;
        }
        for (Map.Entry<String, Double> entry : value.entrySet()) {
            Double pressure = entry.getValue();
            if (pressure == null) {
                continue;
            }
            if (pressure < VehicleKeys.TIRE_PRESSURE_MIN || pressure > VehicleKeys.TIRE_PRESSURE_MAX) {
                problems.add(
                        new Problem(
                                "tirePressure",
                                "tirePressure."
                                        + entry.getKey()
                                        + " 必须在 0~1000 kPa 之间"));
            }
        }
    }

    private record Problem(String property, String message) {}
}
