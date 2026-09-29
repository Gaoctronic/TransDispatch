"""车辆相关接口。

- ``PUT    /vehicles/{vehicle_id}/location``  设置/更新车辆位置
- ``PATCH  /vehicles/{vehicle_id}/status``    设置/更新车辆其他状态
- ``GET    /vehicles/{vehicle_id}``           查询车辆信息
"""

from __future__ import annotations

from typing import Annotated

from fastapi import APIRouter, Path, status

from app.api.deps import StoreDep
from app.core.exceptions import VehicleNotFoundError
from app.core.response import ApiResponse, success
from app.schemas.vehicle import (
    VehicleDetailData,
    VehicleLocation,
    VehicleLocationData,
    VehicleStatusData,
    VehicleStatusPatch,
)

router = APIRouter(prefix="/vehicles", tags=["车辆"])

VehicleId = Annotated[
    str,
    Path(
        description="车辆唯一标识",
        min_length=1,
        max_length=64,
        pattern=r"^[A-Za-z0-9._:-]+$",
        examples=["VH-1001"],
    ),
]


@router.put(
    "/{vehicle_id}/location",
    response_model=ApiResponse[VehicleLocationData],
    status_code=status.HTTP_200_OK,
    summary="设置/更新车辆位置",
    description=(
        "全量覆盖式写入车辆位置：请求体里的字段会整体替换旧位置。\n\n"
        "- 车辆不存在时自动创建（upsert 语义）；\n"
        "- 每次写入成功，车辆 `version` 自增 1；\n"
        "- `timestamp` 不传则使用服务器当前时间；\n"
        "- 未在请求体中出现的字段按默认值处理（不会保留上一次的旧值）。"
    ),
    responses={
        200: {"description": "写入成功"},
        422: {"description": "请求体参数校验失败（code=40001）"},
    },
)
def set_vehicle_location(
    vehicle_id: VehicleId,
    payload: VehicleLocation,
    store: StoreDep,
) -> ApiResponse[VehicleLocationData]:
    """设置或更新车辆位置。"""

    record, created = store.set_location(vehicle_id, payload)
    return success(
        VehicleLocationData(
            vehicleId=record.vehicleId,
            version=record.version,
            location=record.location,
            updatedAt=record.updatedAt,
            created=created,
        )
    )


@router.patch(
    "/{vehicle_id}/status",
    response_model=ApiResponse[VehicleStatusData],
    status_code=status.HTTP_200_OK,
    summary="设置/更新车辆其他状态",
    description=(
        "局部更新车辆状态：**只更新请求里出现的字段**，未出现的字段保持原值不变。\n\n"
        "- 车辆不存在时自动创建（upsert 语义）；\n"
        "- `version` 为简单乐观锁：传入时必须等于当前版本，否则返回 409（code=40901）；\n"
        "- `version` 不传则不校验版本；\n"
        "- 每次写入成功，车辆 `version` 自增 1；\n"
        "- 需要清空某个字段可以显式传 `null`（例如 `\"doors\": null`）。"
    ),
    responses={
        200: {"description": "更新成功"},
        409: {"description": "乐观锁版本冲突（code=40901）"},
        422: {"description": "请求体参数校验失败（code=40001）"},
    },
)
def update_vehicle_status(
    vehicle_id: VehicleId,
    payload: VehicleStatusPatch,
    store: StoreDep,
) -> ApiResponse[VehicleStatusData]:
    """设置或更新车辆其他状态（部分字段更新）。"""

    record, created = store.patch_status(vehicle_id, payload)
    return success(
        VehicleStatusData(
            vehicleId=record.vehicleId,
            version=record.version,
            status=record.status,
            updatedAt=record.updatedAt,
            created=created,
        )
    )


@router.get(
    "/{vehicle_id}",
    response_model=ApiResponse[VehicleDetailData],
    status_code=status.HTTP_200_OK,
    summary="查询车辆信息",
    description=(
        "查询车辆当前的位置、状态与版本号。\n\n"
        "- 车辆从未写入过任何数据时返回 404（code=40401）；\n"
        "- `location` / `status` 为 null 表示该类数据尚未上报。"
    ),
    responses={
        200: {"description": "查询成功"},
        404: {"description": "车辆不存在（code=40401）"},
    },
)
def get_vehicle(
    vehicle_id: VehicleId,
    store: StoreDep,
) -> ApiResponse[VehicleDetailData]:
    """查询车辆信息。"""

    record = store.get(vehicle_id)
    if record is None:
        raise VehicleNotFoundError(vehicle_id)

    return success(
        VehicleDetailData(
            vehicleId=record.vehicleId,
            version=record.version,
            location=record.location,
            status=record.status,
            createdAt=record.createdAt,
            updatedAt=record.updatedAt,
        )
    )
