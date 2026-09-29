package com.example.vehicleapi.core.error;

import com.example.vehicleapi.core.web.ErrorCodes;
import java.util.Map;

/** 车辆不存在（HTTP 404 / code 40401）。 */
public class VehicleNotFoundException extends VehicleApiException {

    public VehicleNotFoundException(String vehicleId) {
        super(
                404,
                ErrorCodes.NOT_FOUND,
                "车辆不存在：" + vehicleId,
                Map.of("vehicleId", vehicleId));
    }
}
