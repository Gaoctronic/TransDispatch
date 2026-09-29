package com.example.vehicleapi.service;

import com.example.vehicleapi.core.error.VersionConflictException;
import com.example.vehicleapi.domain.vehicle.VehicleLocation;
import com.example.vehicleapi.domain.vehicle.VehicleStatus;
import com.example.vehicleapi.domain.vehicle.VehicleStatusPatch;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

/**
 * 线程安全的内存车辆仓库（最小可用版本）。
 *
 * <p>设计要点（与 Python 版本一致）：
 *
 * <ul>
 *   <li>单进程内 {@code Map} + {@link ReentrantLock}，保证并发请求下的读写安全；
 *   <li>每次写入都会让车辆 {@code version} 自增 1，用作简单乐观锁；
 *   <li>读接口一律返回深拷贝，避免调用方误改内存中的原始对象；
 *   <li>服务重启数据即丢失，多进程（如多实例部署）之间不共享数据。
 * </ul>
 */
@Component
public class InMemoryVehicleStore {

    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, VehicleRecord> vehicles = new HashMap<>();

    // ------------------------------------------------------------------ 查询

    /** 按车辆 ID 查询，返回副本；不存在返回 {@code null}。 */
    public VehicleRecord get(String vehicleId) {
        lock.lock();
        try {
            VehicleRecord record = vehicles.get(vehicleId);
            return record == null ? null : record.copy();
        } finally {
            lock.unlock();
        }
    }

    /** 车辆是否存在。 */
    public boolean exists(String vehicleId) {
        lock.lock();
        try {
            return vehicles.containsKey(vehicleId);
        } finally {
            lock.unlock();
        }
    }

    /** 当前内存中的车辆数量。 */
    public int count() {
        lock.lock();
        try {
            return vehicles.size();
        } finally {
            lock.unlock();
        }
    }

    /** 返回所有车辆 ID（按字典序，便于测试断言）。 */
    public List<String> listVehicleIds() {
        lock.lock();
        try {
            List<String> ids = new ArrayList<>(vehicles.keySet());
            Collections.sort(ids);
            return ids;
        } finally {
            lock.unlock();
        }
    }

    /** 清空所有数据（测试 / 关闭服务时使用）。 */
    public void clear() {
        lock.lock();
        try {
            vehicles.clear();
        } finally {
            lock.unlock();
        }
    }

    // ------------------------------------------------------------------ 写入

    /** 设置 / 更新车辆位置（全量覆盖）。 */
    public WriteResult setLocation(String vehicleId, VehicleLocation location) {
        lock.lock();
        try {
            Created created = getOrCreate(vehicleId);
            VehicleRecord record = created.record();

            record.setLocation(location.copy());
            record.setVersion(record.getVersion() + 1);
            record.setUpdatedAt(Instant.now());
            vehicles.put(vehicleId, record);

            return new WriteResult(record.copy(), created.created());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 局部更新车辆状态。
     *
     * <p>只覆盖请求里显式传入的字段，未传字段保持原值。{@code version} 作为乐观锁：传了就必须等于当前版本，
     * 否则抛出 {@link VersionConflictException}（HTTP 409）。
     */
    public WriteResult patchStatus(String vehicleId, VehicleStatusPatch patch) {
        lock.lock();
        try {
            Created created = getOrCreate(vehicleId);
            VehicleRecord record = created.record();

            // version 传了才校验：显式传 null 视为「不校验」，与 Python 版本保持一致
            Integer providedVersion = patch.getVersion();
            if (providedVersion != null && providedVersion != record.getVersion()) {
                throw new VersionConflictException(record.getVersion(), providedVersion);
            }

            VehicleStatus merged =
                    record.getStatus() == null ? new VehicleStatus() : record.getStatus().copy();

            if (patch.isProvided("ignitionOn")) {
                merged.setIgnitionOn(patch.getIgnitionOn());
            }
            if (patch.isProvided("gear")) {
                merged.setGear(patch.getGear());
            }
            if (patch.isProvided("online")) {
                merged.setOnline(patch.getOnline());
            }
            if (patch.isProvided("charging")) {
                merged.setCharging(patch.getCharging());
            }
            if (patch.isProvided("batteryLevel")) {
                merged.setBatteryLevel(patch.getBatteryLevel());
            }
            if (patch.isProvided("fuelLevel")) {
                merged.setFuelLevel(patch.getFuelLevel());
            }
            if (patch.isProvided("mileage")) {
                merged.setMileage(patch.getMileage());
            }
            if (patch.isProvided("doors")) {
                merged.setDoors(patch.getDoors());
            }
            if (patch.isProvided("windows")) {
                merged.setWindows(patch.getWindows());
            }
            if (patch.isProvided("tirePressure")) {
                merged.setTirePressure(patch.getTirePressure());
            }
            if (patch.isProvided("faultCodes")) {
                merged.setFaultCodes(patch.getFaultCodes());
            }
            if (patch.isProvided("timestamp")) {
                merged.setTimestamp(patch.getTimestamp());
            }
            if (patch.isProvided("eventId")) {
                merged.setEventId(patch.getEventId());
            }
            if (patch.isProvided("extra")) {
                merged.setExtra(patch.getExtra());
            }

            // 首次创建且未显式提供 timestamp 时，取服务器当前时间
            if (merged.getTimestamp() == null) {
                merged.setTimestamp(Instant.now());
            }

            int newVersion = record.getVersion() + 1;
            merged.setVersion(newVersion);

            record.setStatus(merged);
            record.setVersion(newVersion);
            record.setUpdatedAt(Instant.now());
            vehicles.put(vehicleId, record);

            return new WriteResult(record.copy(), created.created());
        } finally {
            lock.unlock();
        }
    }

    // ---------------------------------------------------------------- 内部

    /** 取出记录，不存在则创建一个尚未落库的候选记录。**必须在持锁状态下调用。** */
    private Created getOrCreate(String vehicleId) {
        VehicleRecord existing = vehicles.get(vehicleId);
        if (existing != null) {
            return new Created(existing, false);
        }
        return new Created(VehicleRecord.create(vehicleId, Instant.now()), true);
    }

    /** 写入结果：记录 + 本次是否新建了车辆。 */
    public record WriteResult(VehicleRecord record, boolean created) {}

    private record Created(VehicleRecord record, boolean created) {}
}
