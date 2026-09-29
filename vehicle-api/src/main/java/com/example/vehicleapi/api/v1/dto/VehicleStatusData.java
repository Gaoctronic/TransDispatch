package com.example.vehicleapi.api.v1.dto;

import com.example.vehicleapi.domain.vehicle.VehicleStatus;
import java.time.Instant;

/** PATCH /vehicles/{vehicle_id}/status 的响应数据。 */
public class VehicleStatusData {

    private final String vehicleId;
    private final int version;
    private final VehicleStatus status;
    private final Instant updatedAt;
    private final boolean created;

    public VehicleStatusData(
            String vehicleId,
            int version,
            VehicleStatus status,
            Instant updatedAt,
            boolean created) {
        this.vehicleId = vehicleId;
        this.version = version;
        this.status = status;
        this.updatedAt = updatedAt;
        this.created = created;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public int getVersion() {
        return version;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isCreated() {
        return created;
    }
}
