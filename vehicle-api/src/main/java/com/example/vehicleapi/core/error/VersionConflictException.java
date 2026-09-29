package com.example.vehicleapi.core.error;

import com.example.vehicleapi.core.web.ErrorCodes;
import java.util.LinkedHashMap;
import java.util.Map;

/** 乐观锁版本冲突：客户端携带的 version 不是最新版本（HTTP 409 / code 40901）。 */
public class VersionConflictException extends VehicleApiException {

    public VersionConflictException(int currentVersion, int providedVersion) {
        super(
                409,
                ErrorCodes.VERSION_CONFLICT,
                "版本冲突：请求携带的 version 不是当前最新版本，请重新查询车辆信息后重试",
                details(currentVersion, providedVersion));
    }

    private static Map<String, Object> details(int currentVersion, int providedVersion) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("currentVersion", currentVersion);
        data.put("providedVersion", providedVersion);
        return data;
    }
}
