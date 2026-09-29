package com.example.vehicleapi.service;

import com.example.vehicleapi.domain.vehicle.VehicleLocation;
import com.example.vehicleapi.domain.vehicle.VehicleStatus;
import java.time.Instant;

/**
 * 一辆车在内存中的完整记录。
 *
 * <p>对应 Python 版本的 {@code app/services/store.py::VehicleRecord}。
 */
public class VehicleRecord {

    private String vehicleId;
    private int version;
    private VehicleLocation location;
    private VehicleStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public VehicleRecord() {}

    public VehicleRecord(
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

    /** 新建一辆尚未写入任何数据的车辆记录。 */
    public static VehicleRecord create(String vehicleId, Instant now) {
        return new VehicleRecord(vehicleId, 0, null, null, now, now);
    }

    /** 读取接口一律返回副本，避免调用方误改内存中的原始对象。 */
    public VehicleRecord copy() {
        return new VehicleRecord(
                vehicleId,
                version,
                location == null ? null : location.copy(),
                status == null ? null : status.copy(),
                createdAt,
                updatedAt);
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public VehicleLocation getLocation() {
        return location;
    }

    public void setLocation(VehicleLocation location) {
        this.location = location;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
