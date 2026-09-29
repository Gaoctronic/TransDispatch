"""GET /api/v1/vehicles/{vehicle_id} 与整体流程的测试。"""

from __future__ import annotations

import pytest
from fastapi.testclient import TestClient

API = "/api/v1"


def test_get_vehicle_after_writes(client: TestClient) -> None:
    vehicle_id = "VH-3003"

    location_response = client.put(
        f"{API}/vehicles/{vehicle_id}/location",
        json={
            "longitude": 113.941452,
            "latitude": 22.546761,
            "altitude": 20.0,
            "speed": 0.0,
            "heading": 0.0,
            "accuracy": 8.0,
            "coordType": "GCJ02",
            "source": "FUSION",
            "timestamp": "2026-09-29T05:00:00.000Z",
            "eventId": "evt-loc-flow",
        },
    )
    assert location_response.status_code == 200

    status_response = client.patch(
        f"{API}/vehicles/{vehicle_id}/status",
        json={
            "ignitionOn": False,
            "gear": "P",
            "online": True,
            "charging": True,
            "batteryLevel": 66.0,
            "mileage": 45678.9,
            "eventId": "evt-status-flow",
        },
    )
    assert status_response.status_code == 200

    response = client.get(f"{API}/vehicles/{vehicle_id}")

    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 0

    data = body["data"]
    assert data["vehicleId"] == vehicle_id
    assert data["version"] == 2
    assert data["location"]["coordType"] == "GCJ02"
    assert data["location"]["source"] == "FUSION"
    assert data["location"]["timestamp"] == "2026-09-29T05:00:00.000Z"
    assert data["status"]["batteryLevel"] == pytest.approx(66.0)
    assert data["status"]["charging"] is True
    assert data["status"]["version"] == 2
    assert data["createdAt"].endswith("Z")
    assert data["updatedAt"].endswith("Z")


def test_get_vehicle_with_only_location(client: TestClient) -> None:
    vehicle_id = "VH-4004"
    client.put(
        f"{API}/vehicles/{vehicle_id}/location",
        json={"longitude": 1.0, "latitude": 2.0},
    )

    data = client.get(f"{API}/vehicles/{vehicle_id}").json()["data"]

    assert data["location"] is not None
    assert data["status"] is None
    assert data["version"] == 1


def test_get_vehicle_with_only_status(client: TestClient) -> None:
    vehicle_id = "VH-5005"
    client.patch(f"{API}/vehicles/{vehicle_id}/status", json={"online": True})

    data = client.get(f"{API}/vehicles/{vehicle_id}").json()["data"]

    assert data["location"] is None
    assert data["status"] is not None
    assert data["version"] == 1


def test_vehicles_are_isolated(client: TestClient) -> None:
    client.patch(f"{API}/vehicles/VH-A/status", json={"online": True, "batteryLevel": 10.0})
    client.patch(f"{API}/vehicles/VH-B/status", json={"online": False, "batteryLevel": 90.0})

    data_a = client.get(f"{API}/vehicles/VH-A").json()["data"]
    data_b = client.get(f"{API}/vehicles/VH-B").json()["data"]

    assert data_a["status"]["batteryLevel"] == pytest.approx(10.0)
    assert data_a["status"]["online"] is True
    assert data_b["status"]["batteryLevel"] == pytest.approx(90.0)
    assert data_b["status"]["online"] is False
