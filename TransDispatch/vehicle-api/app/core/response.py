"""统一响应格式与业务状态码。

所有接口（包括错误响应）都返回如下结构：

    {"code": 0, "message": "success", "data": ...}
"""

from __future__ import annotations

from typing import Any, Generic, TypeVar

from pydantic import BaseModel, Field

T = TypeVar("T")

# 业务状态码（与 HTTP 状态码解耦，统一放在响应体 code 字段里）
CODE_SUCCESS = 0
CODE_VALIDATION_ERROR = 40001
CODE_NOT_FOUND = 40401
CODE_VERSION_CONFLICT = 40901
CODE_INTERNAL_ERROR = 50000

MESSAGE_SUCCESS = "success"


class ApiResponse(BaseModel, Generic[T]):
    """统一响应外壳。"""

    code: int = Field(default=CODE_SUCCESS, description="业务状态码，0 表示成功")
    message: str = Field(default=MESSAGE_SUCCESS, description="提示信息")
    data: T | None = Field(default=None, description="业务数据，出错时为错误详情或 null")


def success(data: T | None = None, message: str = MESSAGE_SUCCESS) -> ApiResponse[T]:
    """构造成功响应。"""

    return ApiResponse[T](code=CODE_SUCCESS, message=message, data=data)


def error(code: int, message: str, data: Any = None) -> dict[str, Any]:
    """构造错误响应体（供异常处理器使用）。"""

    return {"code": code, "message": message, "data": data}
