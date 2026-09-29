"""应用入口。

启动方式：

- ``uvicorn app.main:app --reload``
- 或 ``python -m app.main``
"""

from __future__ import annotations

import logging
from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
from typing import Any

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.v1.router import api_router
from app.core.config import Settings, get_settings
from app.core.errors import register_exception_handlers
from app.core.response import ApiResponse, success
from app.services.store import InMemoryVehicleStore

logger = logging.getLogger("vehicle_api")


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    """应用生命周期：启动时创建内存仓库，关闭时清理。"""

    app.state.store = InMemoryVehicleStore()
    logger.info("vehicle-api 启动完成：内存存储已初始化（服务重启后数据会丢失）")
    try:
        yield
    finally:
        app.state.store.clear()
        logger.info("vehicle-api 已关闭：内存数据已清理")


def create_app(settings: Settings | None = None) -> FastAPI:
    """创建 FastAPI 应用实例（工厂函数，便于测试与多环境部署）。"""

    current = settings or get_settings()

    app = FastAPI(
        title=current.app_name,
        version=current.app_version,
        description=current.app_description,
        docs_url="/docs",
        redoc_url="/redoc",
        openapi_url="/openapi.json",
        lifespan=lifespan,
        license_info={"name": "MIT", "url": "https://opensource.org/licenses/MIT"},
        openapi_tags=[
            {"name": "车辆", "description": "车辆位置与状态的写入、查询接口"},
            {"name": "系统", "description": "健康检查等运维接口"},
        ],
    )

    # 演示方便：允许任意来源跨域访问（生产环境请收敛 allow_origins）
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=False,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    register_exception_handlers(app)
    app.include_router(api_router, prefix=current.api_prefix)

    @app.get(
        "/health",
        tags=["系统"],
        summary="健康检查",
        response_model=ApiResponse[dict[str, Any]],
    )
    async def health() -> ApiResponse[dict[str, Any]]:
        """存活探针。"""

        return success(
            {
                "status": "ok",
                "app": current.app_name,
                "version": current.app_version,
            }
        )

    return app


app = create_app()


if __name__ == "__main__":  # pragma: no cover
    import uvicorn

    _settings = get_settings()
    logging.basicConfig(level=logging.DEBUG if _settings.debug else logging.INFO)
    uvicorn.run(
        "app.main:app",
        host=_settings.host,
        port=_settings.port,
        reload=_settings.debug,
    )
