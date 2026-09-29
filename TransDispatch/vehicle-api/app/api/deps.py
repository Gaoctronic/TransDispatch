"""FastAPI 依赖注入。"""

from __future__ import annotations

from typing import Annotated

from fastapi import Depends, Request

from app.services.store import InMemoryVehicleStore


def get_store(request: Request) -> InMemoryVehicleStore:
    """从应用状态里取出内存仓库（在 lifespan 中初始化）。"""

    return request.app.state.store


StoreDep = Annotated[InMemoryVehicleStore, Depends(get_store)]
