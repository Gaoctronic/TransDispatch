"""全局异常处理器：保证任何错误都返回统一响应格式。"""

from __future__ import annotations

import logging
from typing import Any

from fastapi import FastAPI, Request
from fastapi.encoders import jsonable_encoder
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException

from app.core.exceptions import VehicleApiError
from app.core.response import CODE_INTERNAL_ERROR, CODE_VALIDATION_ERROR, error

logger = logging.getLogger("vehicle_api")


def _validation_details(exc: RequestValidationError) -> list[dict[str, Any]]:
    """把 Pydantic / FastAPI 的校验错误整理成稳定、易读的结构。"""

    details: list[dict[str, Any]] = []
    for item in exc.errors():
        location = ".".join(str(part) for part in item.get("loc", ()))
        details.append(
            {
                "field": location or "body",
                "type": item.get("type", "value_error"),
                "message": item.get("msg", "参数不合法"),
            }
        )
    return details


def register_exception_handlers(app: FastAPI) -> None:
    """注册所有异常处理器。"""

    @app.exception_handler(VehicleApiError)
    async def handle_vehicle_api_error(
        request: Request, exc: VehicleApiError
    ) -> JSONResponse:
        return JSONResponse(
            status_code=exc.status_code,
            content=jsonable_encoder(exc.to_payload()),
        )

    @app.exception_handler(RequestValidationError)
    async def handle_request_validation_error(
        request: Request, exc: RequestValidationError
    ) -> JSONResponse:
        return JSONResponse(
            status_code=422,
            content=error(
                CODE_VALIDATION_ERROR,
                "请求参数校验失败",
                {"errors": _validation_details(exc)},
            ),
        )

    @app.exception_handler(StarletteHTTPException)
    async def handle_http_exception(
        request: Request, exc: StarletteHTTPException
    ) -> JSONResponse:
        detail = exc.detail
        message = detail if isinstance(detail, str) else "HTTP 请求错误"
        headers = getattr(exc, "headers", None)
        return JSONResponse(
            status_code=exc.status_code,
            content=error(exc.status_code, message, None if isinstance(detail, str) else detail),
            headers=headers,
        )

    @app.exception_handler(Exception)
    async def handle_unexpected_error(request: Request, exc: Exception) -> JSONResponse:
        logger.exception("未处理的服务端异常: %s %s", request.method, request.url.path)
        return JSONResponse(
            status_code=500,
            content=error(CODE_INTERNAL_ERROR, "服务器内部错误", None),
        )
