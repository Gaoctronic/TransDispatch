package com.example.vehicleapi.domain.vehicle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 车辆状态（PATCH 局部更新：只更新请求里出现的字段）。
 *
 * <p>对应 Python 版本的 {@code app/schemas/vehicle.py::VehicleStatusPatch}。
 *
 * <p>注意：Python 通过 {@code model_fields_set} 区分「字段未传」与「字段显式传了 null」。
 * Java 这边由每个 setter 记录「本次请求出现过的字段名」来实现同样的语义，
 * Jackson 只有在 JSON 里真正出现该字段时才会调用对应的 setter。
 */
@AllowedStatusKeys
public class VehicleStatusPatch {

    private final Set<String> providedFields = new LinkedHashSet<>();

    private Boolean ignitionOn;
    private Gear gear;
    private Boolean online;
    private Boolean charging;

    @DecimalMin(value = "0.0", message = "Input should be greater than or equal to 0")
    @DecimalMax(value = "100.0", message = "Input should be less than or equal to 100")
    private Double batteryLevel;

    @DecimalMin(value = "0.0", message = "Input should be greater than or equal to 0")
    @DecimalMax(value = "100.0", message = "Input should be less than or equal to 100")
    private Double fuelLevel;

    @DecimalMin(value = "0.0", message = "Input should be greater than or equal to 0")
    private Double mileage;

    private Map<String, SwitchState> doors;
    private Map<String, SwitchState> windows;
    private Map<String, Double> tirePressure;

    @Size(max = 100, message = "List should have at most 100 items")
    private List<String> faultCodes;

    private Instant timestamp;

    @Min(value = 0, message = "Input should be greater than or equal to 0")
    private Integer version;

    @Size(min = 1, max = 64, message = "String should have at least 1 and at most 64 characters")
    private String eventId;

    private Map<String, Object> extra;

    /** 本次请求中真正出现过的字段名（用于实现局部更新语义）。 */
    @JsonIgnore
    public Set<String> providedFields() {
        return providedFields;
    }

    /** 判断某个字段是否在本次请求中出现过。 */
    @JsonIgnore
    public boolean isProvided(String field) {
        return providedFields.contains(field);
    }

    private void provided(String field) {
        providedFields.add(field);
    }

    public Boolean getIgnitionOn() {
        return ignitionOn;
    }

    public void setIgnitionOn(Boolean ignitionOn) {
        provided("ignitionOn");
        this.ignitionOn = ignitionOn;
    }

    public Gear getGear() {
        return gear;
    }

    public void setGear(Gear gear) {
        provided("gear");
        this.gear = gear;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        provided("online");
        this.online = online;
    }

    public Boolean getCharging() {
        return charging;
    }

    public void setCharging(Boolean charging) {
        provided("charging");
        this.charging = charging;
    }

    public Double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Double batteryLevel) {
        provided("batteryLevel");
        this.batteryLevel = batteryLevel;
    }

    public Double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(Double fuelLevel) {
        provided("fuelLevel");
        this.fuelLevel = fuelLevel;
    }

    public Double getMileage() {
        return mileage;
    }

    public void setMileage(Double mileage) {
        provided("mileage");
        this.mileage = mileage;
    }

    public Map<String, SwitchState> getDoors() {
        return doors;
    }

    public void setDoors(Map<String, SwitchState> doors) {
        provided("doors");
        this.doors = doors;
    }

    public Map<String, SwitchState> getWindows() {
        return windows;
    }

    public void setWindows(Map<String, SwitchState> windows) {
        provided("windows");
        this.windows = windows;
    }

    public Map<String, Double> getTirePressure() {
        return tirePressure;
    }

    public void setTirePressure(Map<String, Double> tirePressure) {
        provided("tirePressure");
        this.tirePressure = tirePressure;
    }

    public List<String> getFaultCodes() {
        return faultCodes;
    }

    public void setFaultCodes(List<String> faultCodes) {
        provided("faultCodes");
        this.faultCodes = faultCodes;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        provided("timestamp");
        this.timestamp = timestamp;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        provided("version");
        this.version = version;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        provided("eventId");
        this.eventId = eventId;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        provided("extra");
        this.extra = extra;
    }
}
