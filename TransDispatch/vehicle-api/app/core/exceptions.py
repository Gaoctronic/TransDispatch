"""领域异常：由全局异常处理器转换成统一响应格式。"""

from __future__ import annotations

from typing import Any

from app.core.response import (
    CODE_INTERNAL_ERROR,
    CODE_NOT_FOUND,
    CODE_VALIDATION_ERROR,
    CODE_VERSION_CONFLICT,
)


class VehicleApiError(Exception):
    """业务异常基类。"""

    status_code: int = 400
    code: int = CODE_VALIDATION_ERROR
    message: str = "请求处理失败"

    def __init__(
        self,
        message: str | None = None,
        *,
        data: Any = None,
        code: int | None = None,
        status_code: int | None = None,
    ) -> None:
        self.message = message or self.message
        self.data = data
        if code is not None:
            self.code = code
        if status_code is not None:
            self.status_code = status_code
        super().__init__(self.message)

    def to_payload(self) -> dict[str, Any]:
        """转换成统一响应体。"""

        return {"code": self.code, "message": self.message, "data": self.data}


class VehicleNotFoundError(VehicleApiError):
    """车辆不存在。"""

    status_code = 404
    code = CODE_NOT_FOUND
    message = "vehicle not found"

    def __init__(self, vehicle_id: str) -> None:
        super().__init__(
            f"车辆不存在：{vehicle_id}",
            data={"vehicleId": vehicle_id},
        )


class VersionConflictError(VehicleApiError):
    """乐观锁版本冲突（客户端携带的 version 不是最新版本）。"""

    status_code = 409
    code = CODE_VERSION_CONFLICT
    message = "version conflict"

    def __init__(self, current_version: int, provided_version: int) -> None:
        super().__init__(
            "版本冲突：请求携带的 version 不是当前最新版本，请重新查询车辆信息后重试",
            data={
                "currentVersion": current_version,
                "providedVersion": provided_version,
            },
        )


class InternalError(VehicleApiError):
    """未预期的服务端错误。"""

    status_code = 500
    code = CODE_INTERNAL_ERROR
    message = "服务器内部错误"
