"""PUT /api/v1/vehicles/{vehicle_id}/location 的测试。"""

from __future__ import annotations

import pytest
from fastapi.testclient import TestClient

API = "/api/v1"
VEHICLE_ID = "VH-1001"
LOCATION_URL = f"{API}/vehicles/{VEHICLE_ID}/location"
QUERY_URL = f"{API}/vehicles/{VEHICLE_ID}"


def build_location(**overrides: object) -> dict[str, object]:
    """构造一份合法的位置请求体。"""

    payload: dict[str, object] = {
        "longitude": 116.397128,
        "latitude": 39.916527,
        "altitude": 45.5,
        "speed": 32.4,
        "heading": 88.0,
        "accuracy": 5.0,
        "coordType": "WGS84",
        "source": "GPS",
        "timestamp": "2026-09-29T02:15:30.000Z",
        "eventId": "evt-location-1",
    }
    payload.update(overrides)
    return payload


def test_put_location_creates_vehicle(client: TestClient) -> None:
    response = client.put(LOCATION_URL, json=build_location())

    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 0
    assert body["message"] == "success"

    data = body["data"]
    assert data["vehicleId"] == VEHICLE_ID
    assert data["version"] == 1
    assert data["created"] is True
    assert data["updatedAt"].endswith("Z")

    location = data["location"]
    assert location["longitude"] == pytest.approx(116.397128)
    assert location["latitude"] == pytest.approx(39.916527)
    assert location["altitude"] == pytest.approx(45.5)
    assert location["speed"] == pytest.approx(32.4)
    assert location["heading"] == pytest.approx(88.0)
    assert location["accuracy"] == pytest.approx(5.0)
    assert location["coordType"] == "WGS84"
    assert location["source"] == "GPS"
    assert location["timestamp"] == "2026-09-29T02:15:30.000Z"
    assert location["eventId"] == "evt-location-1"


def test_put_location_overwrites_previous_location(client: TestClient) -> None:
    first = client.put(LOCATION_URL, json=build_location())
    assert first.status_code == 200

    second_payload = build_location(
        longitude=121.473701,
        latitude=31.230416,
        speed=0.0,
        eventId="evt-location-2",
        timestamp="2026-09-29T03:00:00.000Z",
    )
    # 这次不传 altitude / heading，用于验证位置接口是"全量覆盖"而不是"局部更新"
    second_payload.pop("altitude")
    second_payload.pop("heading")

    second = client.put(LOCATION_URL, json=second_payload)

    assert second.status_code == 200
    data = second.json()["data"]
    assert data["created"] is False
    assert data["version"] == 2
    assert data["location"]["longitude"] == pytest.approx(121.473701)
    assert data["location"]["eventId"] == "evt-location-2"

    # 位置是整体覆盖：未传的字段回到默认值，不会保留上一份数据
    assert data["location"]["altitude"] is None
    assert data["location"]["heading"] is None

    detail = client.get(QUERY_URL).json()["data"]
    assert detail["version"] == 2
    assert detail["location"]["longitude"] == pytest.approx(121.473701)


def test_put_location_timestamp_defaults_to_server_time(client: TestClient) -> None:
    payload = build_location()
    payload.pop("timestamp")

    response = client.put(LOCATION_URL, json=payload)

    assert response.status_code == 200
    timestamp = response.json()["data"]["location"]["timestamp"]
    assert timestamp.endswith("Z")
    assert len(timestamp) == len("2026-09-29T02:15:30.000Z")


def test_put_location_without_optional_fields(client: TestClient) -> None:
    response = client.put(
        LOCATION_URL,
        json={"longitude": 116.397128, "latitude": 39.916527},
    )

    assert response.status_code == 200
    location = response.json()["data"]["location"]
    assert location["coordType"] == "WGS84"
    assert location["source"] == "UNKNOWN"
    assert location["eventId"] is None
    assert location["speed"] is None


@pytest.mark.parametrize(
    ("field", "value", "expected_field"),
    [
        ("longitude", 200.0, "body.longitude"),
        ("latitude", -91.0, "body.latitude"),
        ("heading", 400.0, "body.heading"),
        ("speed", -1.0, "body.speed"),
        ("coordType", "WGS84-X", "body.coordType"),
        ("source", "MAGIC", "body.source"),
        ("eventId", "", "body.eventId"),
    ],
)
def test_put_location_rejects_invalid_values(
    client: TestClient, field: str, value: object, expected_field: str
) -> None:
    response = client.put(LOCATION_URL, json=build_location(**{field: value}))

    assert response.status_code == 422
    body = response.json()
    assert body["code"] == 40001
    assert body["message"] == "请求参数校验失败"
    assert expected_field in {item["field"] for item in body["data"]["errors"]}


def test_put_location_rejects_missing_required_fields(client: TestClient) -> None:
    response = client.put(LOCATION_URL, json={"longitude": 116.397128})

    assert response.status_code == 422
    errors = response.json()["data"]["errors"]
    assert "body.latitude" in {item["field"] for item in errors}


def test_put_location_rejects_unknown_fields(client: TestClient) -> None:
    response = client.put(LOCATION_URL, json=build_location(lontitude=1.0))

    assert response.status_code == 422
    errors = response.json()["data"]["errors"]
    assert "body.lontitude" in {item["field"] for item in errors}


def test_put_location_rejects_invalid_vehicle_id(client: TestClient) -> None:
    response = client.put(
        f"{API}/vehicles/{VEHICLE_ID}%20BAD/location", json=build_location()
    )

    assert response.status_code == 422
    errors = response.json()["data"]["errors"]
    assert "path.vehicle_id" in {item["field"] for item in errors}


def test_get_unknown_vehicle_returns_404(client: TestClient) -> None:
    response = client.get(f"{API}/vehicles/NOT-EXIST")

    assert response.status_code == 404
    assert response.json() == {
        "code": 40401,
        "message": "车辆不存在：NOT-EXIST",
        "data": {"vehicleId": "NOT-EXIST"},
    }
