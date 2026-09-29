package com.example.vehicleapi.domain.vehicle;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 落库后的车辆状态（在局部更新模型基础上，timestamp / version 变为必填）。
 *
 * <p>对应 Python 版本的 {@code app/schemas/vehicle.py::VehicleStatus}。
 *
 * <p>注意：未上报的字段会以 null 输出（例如 {@code "charging": null}），
 * 因此这里不启用 Jackson 的 NON_NULL 序列化特性。
 */
public class VehicleStatus {

    private Boolean ignitionOn;
    private Gear gear;
    private Boolean online;
    private Boolean charging;
    private Double batteryLevel;
    private Double fuelLevel;
    private Double mileage;
    private Map<String, SwitchState> doors;
    private Map<String, SwitchState> windows;
    private Map<String, Double> tirePressure;
    private List<String> faultCodes;
    private Instant timestamp;
    private Integer version;
    private String eventId;
    private Map<String, Object> extra;

    /** 浅拷贝即可：本类所有字段类型都是不可变对象或其元素不会在本服务内被修改。 */
    public VehicleStatus copy() {
        VehicleStatus copy = new VehicleStatus();
        copy.ignitionOn = this.ignitionOn;
        copy.gear = this.gear;
        copy.online = this.online;
        copy.charging = this.charging;
        copy.batteryLevel = this.batteryLevel;
        copy.fuelLevel = this.fuelLevel;
        copy.mileage = this.mileage;
        copy.doors = this.doors;
        copy.windows = this.windows;
        copy.tirePressure = this.tirePressure;
        copy.faultCodes = this.faultCodes;
        copy.timestamp = this.timestamp;
        copy.version = this.version;
        copy.eventId = this.eventId;
        copy.extra = this.extra;
        return copy;
    }

    public Boolean getIgnitionOn() {
        return ignitionOn;
    }

    public void setIgnitionOn(Boolean ignitionOn) {
        this.ignitionOn = ignitionOn;
    }

    public Gear getGear() {
        return gear;
    }

    public void setGear(Gear gear) {
        this.gear = gear;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public Boolean getCharging() {
        return charging;
    }

    public void setCharging(Boolean charging) {
        this.charging = charging;
    }

    public Double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(Double fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public Double getMileage() {
        return mileage;
    }

    public void setMileage(Double mileage) {
        this.mileage = mileage;
    }

    public Map<String, SwitchState> getDoors() {
        return doors;
    }

    public void setDoors(Map<String, SwitchState> doors) {
        this.doors = doors;
    }

    public Map<String, SwitchState> getWindows() {
        return windows;
    }

    public void setWindows(Map<String, SwitchState> windows) {
        this.windows = windows;
    }

    public Map<String, Double> getTirePressure() {
        return tirePressure;
    }

    public void setTirePressure(Map<String, Double> tirePressure) {
        this.tirePressure = tirePressure;
    }

    public List<String> getFaultCodes() {
        return faultCodes;
    }

    public void setFaultCodes(List<String> faultCodes) {
        this.faultCodes = faultCodes;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}
