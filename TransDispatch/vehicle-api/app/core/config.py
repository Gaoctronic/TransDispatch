"""应用配置。

只读取环境变量，避免引入额外依赖；所有配置项都有安全默认值。
"""

from __future__ import annotations

import os
from dataclasses import dataclass
from functools import lru_cache


def _env_bool(name: str, default: bool) -> bool:
    raw = os.getenv(name)
    if raw is None:
        return default
    return raw.strip().lower() in {"1", "true", "yes", "on"}


@dataclass(frozen=True)
class Settings:
    """服务配置。"""

    app_name: str = "vehicle-api"
    app_version: str = "1.0.0"
    app_description: str = (
        "车辆位置与状态管理后端服务（FastAPI + Pydantic v2 + 内存存储）。\n\n"
        "提供车辆位置的设置/更新、车辆其他状态的局部更新以及车辆信息查询能力。"
    )
    api_prefix: str = "/api/v1"
    host: str = "127.0.0.1"
    port: int = 8000
    debug: bool = False

    @classmethod
    def from_env(cls) -> Settings:
        """从环境变量构造配置。"""

        return cls(
            app_name=os.getenv("APP_NAME", cls.app_name),
            app_version=os.getenv("APP_VERSION", cls.app_version),
            api_prefix=os.getenv("API_PREFIX", cls.api_prefix),
            host=os.getenv("HOST", cls.host),
            port=int(os.getenv("PORT", str(cls.port))),
            debug=_env_bool("DEBUG", cls.debug),
        )


@lru_cache(maxsize=1)
def get_settings() -> Settings:
    """获取（带缓存的）全局配置。"""

    return Settings.from_env()
