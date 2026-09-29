package com.example.vehicleapi.api.v1.dto;

import com.example.vehicleapi.domain.vehicle.VehicleLocation;
import com.example.vehicleapi.domain.vehicle.VehicleStatus;
import java.time.Instant;

/** GET /vehicles/{vehicle_id} 的响应数据。 */
public class VehicleDetailData {

    private final String vehicleId;
    private final int version;
    private final VehicleLocation location;
    private final VehicleStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    public VehicleDetailData(
            String vehicleId,
            int version,
            VehicleLocation location,
            VehicleStatus status,
            Instant createdAt,
            Instant updatedAt) {
        this.vehicleId = vehicleId;
        this.version = version;
        this.location = location;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public int getVersion() {
        return version;
    }

    public VehicleLocation getLocation() {
        return location;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
