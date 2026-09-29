"""通用类型与时间工具。

统一约定：
- 服务内部的时间一律使用「带时区的 UTC datetime」；
- 输出成 JSON 时统一格式化为 ISO 8601，并以 ``Z`` 结尾（例如 ``2026-09-29T02:15:30.000Z``）。
"""

from __future__ import annotations

from datetime import datetime, timezone
from typing import Annotated

from pydantic import AfterValidator, PlainSerializer


def utcnow() -> datetime:
    """返回带时区的当前 UTC 时间。"""

    return datetime.now(timezone.utc)


def ensure_utc(value: datetime) -> datetime:
    """把任意 ``datetime`` 规整为「带时区的 UTC 时间」。

    客户端如果传的是不带时区的时间（例如 ``2026-09-29T10:00:00``），按 UTC 处理。
    """

    if value.tzinfo is None:
        return value.replace(tzinfo=timezone.utc)
    return value.astimezone(timezone.utc)


def format_datetime(value: datetime) -> str:
    """序列化为 ISO 8601 字符串（UTC 用 ``Z`` 结尾）。"""

    return ensure_utc(value).isoformat(timespec="milliseconds").replace("+00:00", "Z")


UTCDateTime = Annotated[
    datetime,
    AfterValidator(ensure_utc),
    PlainSerializer(format_datetime, return_type=str, when_used="json"),
]
"""带自动时区规整与 ISO 8601 序列化的 datetime 类型。"""
