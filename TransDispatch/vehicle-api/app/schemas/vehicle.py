"""车辆接口的请求 / 响应模型（Pydantic v2）。

模型统一使用 ``extra="forbid"``：客户端多传字段会直接返回 422，
这样可以尽早暴露拼写错误，避免"看似成功、其实没生效"的问题。
"""

from __future__ import annotations

from typing import Any

from pydantic import BaseModel, ConfigDict, Field, field_validator

from app.core.types import UTCDateTime, utcnow
from app.models.vehicle import (
    DOOR_KEYS,
    TIRE_KEYS,
    WINDOW_KEYS,
    CoordType,
    Gear,
    LocationSource,
    SwitchState,
)

_BASE_CONFIG = ConfigDict(
    extra="forbid",
    populate_by_name=True,
    str_strip_whitespace=True,
)


class VehicleLocation(BaseModel):
    """车辆位置（PUT 全量覆盖式写入）。"""

    model_config = _BASE_CONFIG

    longitude: float = Field(
        ..., ge=-180.0, le=180.0, description="经度（度）", examples=[116.397128]
    )
    latitude: float = Field(
        ..., ge=-90.0, le=90.0, description="纬度（度）", examples=[39.916527]
    )
    altitude: float | None = Field(
        default=None, ge=-500.0, le=10000.0, description="海拔（米）"
    )
    speed: float | None = Field(
        default=None, ge=0.0, le=1000.0, description="速度（km/h）"
    )
    heading: float | None = Field(
        default=None, ge=0.0, le=360.0, description="航向角（度，正北为 0，顺时针）"
    )
    accuracy: float | None = Field(
        default=None, ge=0.0, le=100000.0, description="定位精度（米）"
    )
    coordType: CoordType = Field(default=CoordType.WGS84, description="坐标系类型")
    source: LocationSource = Field(
        default=LocationSource.UNKNOWN, description="定位来源"
    )
    timestamp: UTCDateTime = Field(
        default_factory=utcnow,
        description="定位采集时间（ISO 8601；不传则取服务器当前时间）",
    )
    eventId: str | None = Field(
        default=None,
        min_length=1,
        max_length=64,
        description="事件 ID，用于链路追踪与幂等对账",
    )


class VehicleStatusPatch(BaseModel):
    """车辆状态（PATCH 局部更新：只更新请求里出现的字段）。"""

    model_config = _BASE_CONFIG

    ignitionOn: bool | None = Field(default=None, description="点火状态")
    gear: Gear | None = Field(default=None, description="挡位")
    online: bool | None = Field(default=None, description="是否在线")
    charging: bool | None = Field(default=None, description="是否充电中")
    batteryLevel: float | None = Field(
        default=None, ge=0.0, le=100.0, description="动力电池剩余电量（%）"
    )
    fuelLevel: float | None = Field(
        default=None, ge=0.0, le=100.0, description="燃油余量（%）"
    )
    mileage: float | None = Field(default=None, ge=0.0, description="总里程（km）")
    doors: dict[str, SwitchState] | None = Field(
        default=None, description="车门状态，键支持 frontLeft/frontRight/rearLeft/rearRight/trunk"
    )
    windows: dict[str, SwitchState] | None = Field(
        default=None, description="车窗状态，键支持 frontLeft/frontRight/rearLeft/rearRight"
    )
    tirePressure: dict[str, float] | None = Field(
        default=None,
        description="胎压（kPa），键支持 frontLeft/frontRight/rearLeft/rearRight/spare",
    )
    faultCodes: list[str] | None = Field(
        default=None, max_length=100, description="故障码列表，传空数组表示清除故障码"
    )
    timestamp: UTCDateTime | None = Field(
        default=None, description="状态采集时间（ISO 8601）"
    )
    version: int | None = Field(
        default=None,
        ge=0,
        description="乐观锁版本号：传了就做校验，不等于当前版本返回 409；不传则不校验",
    )
    eventId: str | None = Field(
        default=None, min_length=1, max_length=64, description="事件 ID"
    )
    extra: dict[str, Any] | None = Field(default=None, description="扩展字段")

    @field_validator("doors", "windows")
    @classmethod
    def _validate_switch_keys(
        cls, value: dict[str, SwitchState] | None, info: Any
    ) -> dict[str, SwitchState] | None:
        if value is None:
            return value
        allowed = DOOR_KEYS if info.field_name == "doors" else WINDOW_KEYS
        unknown = sorted(set(value) - allowed)
        if unknown:
            raise ValueError(
                f"{info.field_name} 含有不支持的键 {unknown}，允许的键为 {sorted(allowed)}"
            )
        return value

    @field_validator("tirePressure")
    @classmethod
    def _validate_tire_pressure(
        cls, value: dict[str, float] | None
    ) -> dict[str, float] | None:
        if value is None:
            return value
        unknown = sorted(set(value) - TIRE_KEYS)
        if unknown:
            raise ValueError(
                f"tirePressure 含有不支持的键 {unknown}，允许的键为 {sorted(TIRE_KEYS)}"
            )
        for key, pressure in value.items():
            if pressure < 0 or pressure > 1000:
                raise ValueError(f"tirePressure.{key} 必须在 0~1000 kPa 之间")
        return value


class VehicleStatus(VehicleStatusPatch):
    """落库后的车辆状态（在局部更新模型基础上，timestamp / version 变为必填）。"""

    timestamp: UTCDateTime = Field(
        default_factory=utcnow, description="状态采集时间（ISO 8601）"
    )
    version: int = Field(
        default=0, ge=0, description="该状态对应的车辆版本号（每次写入 +1）"
    )


class VehicleLocationData(BaseModel):
    """PUT /location 的响应数据。"""

    vehicleId: str = Field(description="车辆唯一标识")
    version: int = Field(description="写入后的车辆版本号")
    location: VehicleLocation = Field(description="最新的车辆位置")
    updatedAt: UTCDateTime = Field(description="服务端写入时间")
    created: bool = Field(description="本次请求是否新建了该车辆")


class VehicleStatusData(BaseModel):
    """PATCH /status 的响应数据。"""

    vehicleId: str = Field(description="车辆唯一标识")
    version: int = Field(description="写入后的车辆版本号")
    status: VehicleStatus = Field(description="更新后的完整车辆状态")
    updatedAt: UTCDateTime = Field(description="服务端写入时间")
    created: bool = Field(description="本次请求是否新建了该车辆")


class VehicleDetailData(BaseModel):
    """GET /vehicles/{vehicle_id} 的响应数据。"""

    vehicleId: str = Field(description="车辆唯一标识")
    version: int = Field(description="当前车辆版本号")
    location: VehicleLocation | None = Field(default=None, description="车辆位置，未上报为 null")
    status: VehicleStatus | None = Field(default=None, description="车辆状态，未上报为 null")
    createdAt: UTCDateTime = Field(description="车辆首次创建时间")
    updatedAt: UTCDateTime = Field(description="最近一次写入时间")
