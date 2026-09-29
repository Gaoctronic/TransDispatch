"""PATCH /api/v1/vehicles/{vehicle_id}/status 的测试。"""

from __future__ import annotations

import pytest
from fastapi.testclient import TestClient

API = "/api/v1"
VEHICLE_ID = "VH-2002"
STATUS_URL = f"{API}/vehicles/{VEHICLE_ID}/status"
QUERY_URL = f"{API}/vehicles/{VEHICLE_ID}"


def test_patch_creates_vehicle_with_only_provided_fields(client: TestClient) -> None:
    response = client.patch(STATUS_URL, json={"batteryLevel": 82.5, "online": True})

    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 0

    data = body["data"]
    assert data["vehicleId"] == VEHICLE_ID
    assert data["created"] is True
    assert data["version"] == 1

    status = data["status"]
    assert status["batteryLevel"] == pytest.approx(82.5)
    assert status["online"] is True
    # 未传入的字段保持为 null，而不是被写成默认值
    assert status["ignitionOn"] is None
    assert status["gear"] is None
    assert status["charging"] is None
    assert status["fuelLevel"] is None
    assert status["mileage"] is None
    assert status["doors"] is None
    assert status["windows"] is None
    assert status["tirePressure"] is None
    assert status["faultCodes"] is None
    assert status["eventId"] is None
    assert status["extra"] is None


def test_patch_only_updates_provided_fields(client: TestClient) -> None:
    client.patch(
        STATUS_URL,
        json={
            "ignitionOn": True,
            "gear": "D",
            "online": True,
            "batteryLevel": 80.0,
            "fuelLevel": 35.0,
            "mileage": 12000.5,
            "doors": {"frontLeft": "CLOSED", "frontRight": "CLOSED"},
            "windows": {"frontLeft": "OPEN"},
            "tirePressure": {"frontLeft": 235.0, "frontRight": 236.5},
            "faultCodes": ["P0301"],
            "eventId": "evt-status-1",
            "extra": {"vendor": "acme"},
        },
    )

    response = client.patch(
        STATUS_URL,
        json={"online": False, "batteryLevel": 77.5},
    )

    assert response.status_code == 200
    data = response.json()["data"]
    assert data["version"] == 2

    status = data["status"]
    # 本次传入的字段被更新
    assert status["online"] is False
    assert status["batteryLevel"] == pytest.approx(77.5)
    # 本次未传入的字段保持原值
    assert status["ignitionOn"] is True
    assert status["gear"] == "D"
    assert status["fuelLevel"] == pytest.approx(35.0)
    assert status["mileage"] == pytest.approx(12000.5)
    assert status["doors"] == {"frontLeft": "CLOSED", "frontRight": "CLOSED"}
    assert status["windows"] == {"frontLeft": "OPEN"}
    assert status["tirePressure"] == {
        "frontLeft": pytest.approx(235.0),
        "frontRight": pytest.approx(236.5),
    }
    assert status["faultCodes"] == ["P0301"]
    assert status["eventId"] == "evt-status-1"
    assert status["extra"] == {"vendor": "acme"}


def test_patch_can_clear_field_with_explicit_null(client: TestClient) -> None:
    client.patch(STATUS_URL, json={"faultCodes": ["P0301"], "doors": {"trunk": "OPEN"}})

    response = client.patch(STATUS_URL, json={"faultCodes": None, "doors": None})

    assert response.status_code == 200
    status = response.json()["data"]["status"]
    assert status["faultCodes"] is None
    assert status["doors"] is None


def test_patch_status_with_all_fields(client: TestClient) -> None:
    payload = {
        "ignitionOn": True,
        "gear": "P",
        "online": True,
        "charging": False,
        "batteryLevel": 100.0,
        "fuelLevel": 0.0,
        "mileage": 888.8,
        "doors": {
            "frontLeft": "CLOSED",
            "frontRight": "CLOSED",
            "rearLeft": "CLOSED",
            "rearRight": "CLOSED",
            "trunk": "CLOSED",
        },
        "windows": {
            "frontLeft": "CLOSED",
            "frontRight": "CLOSED",
            "rearLeft": "CLOSED",
            "rearRight": "CLOSED",
        },
        "tirePressure": {
            "frontLeft": 240.0,
            "frontRight": 241.0,
            "rearLeft": 239.5,
            "rearRight": 240.5,
            "spare": 250.0,
        },
        "faultCodes": [],
        "timestamp": "2026-09-29T04:20:00.000Z",
        "version": 0,
        "eventId": "evt-status-full",
        "extra": {"odometer": 888.8, "tags": ["test"]},
    }

    response = client.patch(STATUS_URL, json=payload)

    assert response.status_code == 200
    status = response.json()["data"]["status"]
    assert status["timestamp"] == "2026-09-29T04:20:00.000Z"
    assert status["version"] == 1
    assert status["gear"] == "P"
    assert status["doors"]["trunk"] == "CLOSED"
    assert status["tirePressure"]["spare"] == pytest.approx(250.0)
    assert status["faultCodes"] == []
    assert status["extra"] == {"odometer": 888.8, "tags": ["test"]}


def test_patch_status_without_version_skips_optimistic_lock(client: TestClient) -> None:
    client.patch(STATUS_URL, json={"online": True})
    client.patch(STATUS_URL, json={"online": False})
    response = client.patch(STATUS_URL, json={"online": True})

    assert response.status_code == 200
    assert response.json()["data"]["version"] == 3


def test_patch_status_with_matching_version_succeeds(client: TestClient) -> None:
    first = client.patch(STATUS_URL, json={"online": True})
    assert first.json()["data"]["version"] == 1

    second = client.patch(STATUS_URL, json={"version": 1, "batteryLevel": 60.0})

    assert second.status_code == 200
    assert second.json()["data"]["version"] == 2


def test_patch_status_with_stale_version_returns_409(client: TestClient) -> None:
    client.patch(STATUS_URL, json={"online": True})  # version -> 1
    client.patch(STATUS_URL, json={"batteryLevel": 90.0})  # version -> 2

    response = client.patch(STATUS_URL, json={"version": 1, "batteryLevel": 10.0})

    assert response.status_code == 409
    assert response.json() == {
        "code": 40901,
        "message": "版本冲突：请求携带的 version 不是当前最新版本，请重新查询车辆信息后重试",
        "data": {"currentVersion": 2, "providedVersion": 1},
    }

    # 冲突后数据没有被修改
    detail = client.get(QUERY_URL).json()["data"]
    assert detail["version"] == 2
    assert detail["status"]["batteryLevel"] == pytest.approx(90.0)


def test_patch_status_with_future_version_returns_409(client: TestClient) -> None:
    client.patch(STATUS_URL, json={"online": True})  # version -> 1

    response = client.patch(STATUS_URL, json={"version": 99, "online": False})

    assert response.status_code == 409
    assert response.json()["data"]["currentVersion"] == 1
    assert response.json()["data"]["providedVersion"] == 99


@pytest.mark.parametrize(
    ("payload", "expected_field"),
    [
        ({"batteryLevel": 150.0}, "body.batteryLevel"),
        ({"batteryLevel": -0.1}, "body.batteryLevel"),
        ({"fuelLevel": 101.0}, "body.fuelLevel"),
        ({"mileage": -5.0}, "body.mileage"),
        ({"gear": "X"}, "body.gear"),
        ({"version": -1}, "body.version"),
        ({"doors": {"front": "OPEN"}}, "body.doors"),
        ({"windows": {"middle": "CLOSED"}}, "body.windows"),
        ({"tirePressure": {"frontLeft": 2000.0}}, "body.tirePressure"),
        ({"faultCodes": "P0301"}, "body.faultCodes"),
        ({"unknownField": 1}, "body.unknownField"),
    ],
)
def test_patch_status_rejects_invalid_payload(
    client: TestClient, payload: dict[str, object], expected_field: str
) -> None:
    response = client.patch(STATUS_URL, json=payload)

    assert response.status_code == 422
    body = response.json()
    assert body["code"] == 40001
    assert expected_field in {item["field"] for item in body["data"]["errors"]}


def test_patch_status_rejects_empty_body(client: TestClient) -> None:
    response = client.patch(STATUS_URL, json={})

    assert response.status_code == 200
    status = response.json()["data"]["status"]
    assert status["version"] == 1
    assert status["online"] is None


def test_patch_status_rejects_non_json_body(client: TestClient) -> None:
    response = client.patch(
        STATUS_URL,
        content=b"online=true",
        headers={"Content-Type": "application/json"},
    )

    assert response.status_code == 422
    assert response.json()["code"] == 40001
