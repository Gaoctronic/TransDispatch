package com.example.vehicleapi.api.v1;

import com.example.vehicleapi.api.v1.dto.VehicleDetailData;
import com.example.vehicleapi.api.v1.dto.VehicleLocationData;
import com.example.vehicleapi.api.v1.dto.VehicleStatusData;
import com.example.vehicleapi.core.error.VehicleNotFoundException;
import com.example.vehicleapi.core.web.ApiResponse;
import com.example.vehicleapi.domain.vehicle.VehicleLocation;
import com.example.vehicleapi.domain.vehicle.VehicleStatusPatch;
import com.example.vehicleapi.service.InMemoryVehicleStore;
import com.example.vehicleapi.service.VehicleRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 车辆相关接口。
 *
 * <ul>
 *   <li>{@code PUT    /vehicles/{vehicle_id}/location}  设置 / 更新车辆位置
 *   <li>{@code PATCH  /vehicles/{vehicle_id}/status}    设置 / 更新车辆其他状态
 *   <li>{@code GET    /vehicles/{vehicle_id}}           查询车辆信息
 * </ul>
 *
 * <p>对应 Python 版本的 {@code app/api/v1/vehicles.py}。
 */
@RestController
@RequestMapping("${api.prefix:/api/v1}/vehicles")
@Tag(name = "车辆", description = "车辆位置与状态的写入、查询接口")
public class VehicleController {

    private final InMemoryVehicleStore store;

    public VehicleController(InMemoryVehicleStore store) {
        this.store = store;
    }

    @PutMapping("/{vehicle_id}/location")
    @Operation(
            summary = "设置/更新车辆位置",
            description =
                    "全量覆盖式写入车辆位置：请求体里的字段会整体替换旧位置。\n\n"
                            + "- 车辆不存在时自动创建（upsert 语义）；\n"
                            + "- 每次写入成功，车辆 `version` 自增 1；\n"
                            + "- `timestamp` 不传则使用服务器当前时间；\n"
                            + "- 未在请求体中出现的字段按默认值处理（不会保留上一次的旧值）。")
    public ApiResponse<VehicleLocationData> setVehicleLocation(
            @Parameter(description = "车辆唯一标识", example = "VH-1001") @PathVariable("vehicle_id")
                    String vehicleId,
            @Valid @RequestBody VehicleLocation payload) {
        VehicleIdValidator.validate(vehicleId);

        InMemoryVehicleStore.WriteResult result = store.setLocation(vehicleId, payload);
        VehicleRecord record = result.record();

        return ApiResponse.success(
                new VehicleLocationData(
                        record.getVehicleId(),
                        record.getVersion(),
                        record.getLocation(),
                        record.getUpdatedAt(),
                        result.created()));
    }

    @PatchMapping("/{vehicle_id}/status")
    @Operation(
            summary = "设置/更新车辆其他状态",
            description =
                    "局部更新车辆状态：**只更新请求里出现的字段**，未出现的字段保持原值不变。\n\n"
                            + "- 车辆不存在时自动创建（upsert 语义）；\n"
                            + "- `version` 为简单乐观锁：传入时必须等于当前版本，否则返回 409（code=40901）；\n"
                            + "- `version` 不传则不校验版本；\n"
                            + "- 每次写入成功，车辆 `version` 自增 1；\n"
                            + "- 需要清空某个字段可以显式传 `null`（例如 `\"doors\": null`）。")
    public ApiResponse<VehicleStatusData> updateVehicleStatus(
            @Parameter(description = "车辆唯一标识", example = "VH-1002") @PathVariable("vehicle_id")
                    String vehicleId,
            @Valid @RequestBody VehicleStatusPatch payload) {
        VehicleIdValidator.validate(vehicleId);

        InMemoryVehicleStore.WriteResult result = store.patchStatus(vehicleId, payload);
        VehicleRecord record = result.record();

        return ApiResponse.success(
                new VehicleStatusData(
                        record.getVehicleId(),
                        record.getVersion(),
                        record.getStatus(),
                        record.getUpdatedAt(),
                        result.created()));
    }

    @GetMapping("/{vehicle_id}")
    @Operation(
            summary = "查询车辆信息",
            description =
                    "查询车辆当前的位置、状态与版本号。\n\n"
                            + "- 车辆从未写入过任何数据时返回 404（code=40401）；\n"
                            + "- `location` / `status` 为 null 表示该类数据尚未上报。")
    public ApiResponse<VehicleDetailData> getVehicle(
            @Parameter(description = "车辆唯一标识", example = "VH-1001") @PathVariable("vehicle_id")
                    String vehicleId) {
        VehicleIdValidator.validate(vehicleId);

        VehicleRecord record = store.get(vehicleId);
        if (record == null) {
            throw new VehicleNotFoundException(vehicleId);
        }

        return ApiResponse.success(
                new VehicleDetailData(
                        record.getVehicleId(),
                        record.getVersion(),
                        record.getLocation(),
                        record.getStatus(),
                        record.getCreatedAt(),
                        record.getUpdatedAt()));
    }
}
