package com.example.vehicleapi.api.v1.dto;

import com.example.vehicleapi.domain.vehicle.VehicleLocation;
import java.time.Instant;

/** PUT /vehicles/{vehicle_id}/location 的响应数据。 */
public class VehicleLocationData {

    private final String vehicleId;
    private final int version;
    private final VehicleLocation location;
    private final Instant updatedAt;
    private final boolean created;

    public VehicleLocationData(
            String vehicleId,
            int version,
            VehicleLocation location,
            Instant updatedAt,
            boolean created) {
        this.vehicleId = vehicleId;
        this.version = version;
        this.location = location;
        this.updatedAt = updatedAt;
        this.created = created;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isCreated() {
        return created;
    }
}
