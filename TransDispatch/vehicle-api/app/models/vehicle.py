"""车辆领域枚举与取值约束。"""

from __future__ import annotations

from enum import Enum


class CoordType(str, Enum):
    """坐标系类型。"""

    WGS84 = "WGS84"
    GCJ02 = "GCJ02"
    BD09 = "BD09"


class LocationSource(str, Enum):
    """定位来源。"""

    GPS = "GPS"
    BEIDOU = "BEIDOU"
    GLONASS = "GLONASS"
    LBS = "LBS"
    WIFI = "WIFI"
    FUSION = "FUSION"
    MANUAL = "MANUAL"
    UNKNOWN = "UNKNOWN"


class Gear(str, Enum):
    """挡位。"""

    P = "P"
    R = "R"
    N = "N"
    D = "D"
    S = "S"
    L = "L"


class SwitchState(str, Enum):
    """开合状态（车门、车窗）。"""

    OPEN = "OPEN"
    CLOSED = "CLOSED"


# 车门 / 车窗 / 胎压允许的键，避免客户端拼写错误导致脏数据
DOOR_KEYS: frozenset[str] = frozenset(
    {"frontLeft", "frontRight", "rearLeft", "rearRight", "trunk"}
)
WINDOW_KEYS: frozenset[str] = frozenset(
    {"frontLeft", "frontRight", "rearLeft", "rearRight"}
)
TIRE_KEYS: frozenset[str] = frozenset(
    {"frontLeft", "frontRight", "rearLeft", "rearRight", "spare"}
)
