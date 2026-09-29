"""内存存储实现（最小可用版本）。

设计要点：
- 单进程内 ``dict`` + ``threading.RLock``，保证并发请求下的读写安全；
- 每次写入都会让车辆 ``version`` 自增 1，用作简单乐观锁；
- 读接口一律返回深拷贝，避免调用方误改内存中的原始对象；
- 服务重启数据即丢失，多进程（如多 worker）之间不共享数据。
"""

from __future__ import annotations

import threading

from pydantic import BaseModel, ConfigDict, Field

from app.core.exceptions import VersionConflictError
from app.core.types import UTCDateTime, utcnow
from app.schemas.vehicle import VehicleLocation, VehicleStatus, VehicleStatusPatch


class VehicleRecord(BaseModel):
    """一辆车在内存中的完整记录。"""

    model_config = ConfigDict(populate_by_name=True)

    vehicleId: str = Field(description="车辆唯一标识")
    version: int = Field(default=0, ge=0, description="乐观锁版本号")
    location: VehicleLocation | None = Field(default=None, description="车辆位置")
    status: VehicleStatus | None = Field(default=None, description="车辆状态")
    createdAt: UTCDateTime = Field(default_factory=utcnow, description="创建时间")
    updatedAt: UTCDateTime = Field(default_factory=utcnow, description="更新时间")


class InMemoryVehicleStore:
    """线程安全的内存车辆仓库。"""

    def __init__(self) -> None:
        self._lock = threading.RLock()
        self._vehicles: dict[str, VehicleRecord] = {}

    # ------------------------------------------------------------------ 查询
    def get(self, vehicle_id: str) -> VehicleRecord | None:
        """按车辆 ID 查询，返回深拷贝；不存在返回 ``None``。"""

        with self._lock:
            record = self._vehicles.get(vehicle_id)
            return record.model_copy(deep=True) if record is not None else None

    def exists(self, vehicle_id: str) -> bool:
        """车辆是否存在。"""

        with self._lock:
            return vehicle_id in self._vehicles

    def count(self) -> int:
        """当前内存中的车辆数量。"""

        with self._lock:
            return len(self._vehicles)

    def list_vehicle_ids(self) -> list[str]:
        """返回所有车辆 ID（按字典序，便于测试断言）。"""

        with self._lock:
            return sorted(self._vehicles)

    def clear(self) -> None:
        """清空所有数据（测试/关闭服务时使用）。"""

        with self._lock:
            self._vehicles.clear()

    # ------------------------------------------------------------------ 写入
    def set_location(
        self, vehicle_id: str, location: VehicleLocation
    ) -> tuple[VehicleRecord, bool]:
        """设置/更新车辆位置（全量覆盖）。

        返回 ``(记录, 是否新建车辆)``。
        """

        with self._lock:
            record, created = self._get_or_create(vehicle_id)
            record.location = location.model_copy(deep=True)
            record.version += 1
            record.updatedAt = utcnow()
            self._vehicles[vehicle_id] = record
            return record.model_copy(deep=True), created

    def patch_status(
        self, vehicle_id: str, patch: VehicleStatusPatch
    ) -> tuple[VehicleRecord, bool]:
        """局部更新车辆状态。

        只覆盖请求里显式传入的字段（``model_fields_set``），未传字段保持原值。
        ``version`` 作为乐观锁：传了就必须等于当前版本，否则抛出
        :class:`VersionConflictError`（HTTP 409）。

        返回 ``(记录, 是否新建车辆)``。
        """

        with self._lock:
            record, created = self._get_or_create(vehicle_id)

            if patch.version is not None and patch.version != record.version:
                raise VersionConflictError(
                    current_version=record.version,
                    provided_version=patch.version,
                )

            updates = patch.model_dump(exclude_unset=True, exclude={"version"})
            merged = record.status.model_dump() if record.status is not None else {}
            merged.update(updates)

            new_version = record.version + 1
            merged["version"] = new_version

            record.status = VehicleStatus(**merged)
            record.version = new_version
            record.updatedAt = utcnow()
            self._vehicles[vehicle_id] = record
            return record.model_copy(deep=True), created

    # ---------------------------------------------------------------- 内部
    def _get_or_create(self, vehicle_id: str) -> tuple[VehicleRecord, bool]:
        """取出记录，不存在则创建。**必须在持锁状态下调用。**"""

        record = self._vehicles.get(vehicle_id)
        if record is not None:
            return record, False

        now = utcnow()
        return (
            VehicleRecord(vehicleId=vehicle_id, version=0, createdAt=now, updatedAt=now),
            True,
        )
