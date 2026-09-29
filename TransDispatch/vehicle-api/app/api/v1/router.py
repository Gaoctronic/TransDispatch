"""v1 路由汇总。"""

from __future__ import annotations

from fastapi import APIRouter

from app.api.v1 import vehicles

api_router = APIRouter()
api_router.include_router(vehicles.router)

__all__ = ["api_router"]
