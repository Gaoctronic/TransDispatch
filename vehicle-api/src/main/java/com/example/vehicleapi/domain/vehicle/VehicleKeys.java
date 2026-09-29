package com.example.vehicleapi.domain.vehicle;

import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Set;

/** 车门 / 车窗 / 胎压允许的键，避免客户端拼写错误导致脏数据。 */
public final class VehicleKeys {

    public static final Set<String> DOOR_KEYS =
            ordered("frontLeft", "frontRight", "rearLeft", "rearRight", "trunk");

    public static final Set<String> WINDOW_KEYS =
            ordered("frontLeft", "frontRight", "rearLeft", "rearRight");

    public static final Set<String> TIRE_KEYS =
            ordered("frontLeft", "frontRight", "rearLeft", "rearRight", "spare");

    /** 胎压的允许区间（kPa）。 */
    public static final double TIRE_PRESSURE_MIN = 0.0;

    public static final double TIRE_PRESSURE_MAX = 1000.0;

    private VehicleKeys() {}

    private static Set<String> ordered(String... keys) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(java.util.List.of(keys)));
    }
}
