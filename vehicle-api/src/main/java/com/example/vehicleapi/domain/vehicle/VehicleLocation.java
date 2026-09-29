package com.example.vehicleapi.domain.vehicle;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * 车辆位置（PUT 全量覆盖式写入）。
 *
 * <p>对应 Python 版本的 {@code app/schemas/vehicle.py::VehicleLocation}。
 */
public class VehicleLocation {

    @NotNull(message = "Field required")
    @DecimalMin(value = "-180.0", message = "Input should be greater than or equal to -180")
    @DecimalMax(value = "180.0", message = "Input should be less than or equal to 180")
    private Double longitude;

    @NotNull(message = "Field required")
    @DecimalMin(value = "-90.0", message = "Input should be greater than or equal to -90")
    @DecimalMax(value = "90.0", message = "Input should be less than or equal to 90")
    private Double latitude;

    @DecimalMin(value = "-500.0", message = "Input should be greater than or equal to -500")
    @DecimalMax(value = "10000.0", message = "Input should be less than or equal to 10000")
    private Double altitude;

    @DecimalMin(value = "0.0", message = "Input should be greater than or equal to 0")
    @DecimalMax(value = "1000.0", message = "Input should be less than or equal to 1000")
    private Double speed;

    @DecimalMin(value = "0.0", message = "Input should be greater than or equal to 0")
    @DecimalMax(value = "360.0", message = "Input should be less than or equal to 360")
    private Double heading;

    @DecimalMin(value = "0.0", message = "Input should be greater than or equal to 0")
    @DecimalMax(value = "100000.0", message = "Input should be less than or equal to 100000")
    private Double accuracy;

    @NotNull(message = "Input should be 'WGS84', 'GCJ02' or 'BD09'")
    private CoordType coordType = CoordType.WGS84;

    @NotNull(message = "Input should be a valid location source")
    private LocationSource source = LocationSource.UNKNOWN;

    @NotNull(message = "Input should be a valid datetime")
    private Instant timestamp = Instant.now();

    @Size(min = 1, max = 64, message = "String should have at least 1 and at most 64 characters")
    private String eventId;

    /** 浅拷贝即可：本类所有字段类型都是不可变对象。 */
    public VehicleLocation copy() {
        VehicleLocation copy = new VehicleLocation();
        copy.longitude = this.longitude;
        copy.latitude = this.latitude;
        copy.altitude = this.altitude;
        copy.speed = this.speed;
        copy.heading = this.heading;
        copy.accuracy = this.accuracy;
        copy.coordType = this.coordType;
        copy.source = this.source;
        copy.timestamp = this.timestamp;
        copy.eventId = this.eventId;
        return copy;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public CoordType getCoordType() {
        return coordType;
    }

    public void setCoordType(CoordType coordType) {
        this.coordType = coordType;
    }

    public LocationSource getSource() {
        return source;
    }

    public void setSource(LocationSource source) {
        this.source = source;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
