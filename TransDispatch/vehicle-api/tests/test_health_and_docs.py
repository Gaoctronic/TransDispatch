"""健康检查、接口文档与统一响应格式的测试。"""

from __future__ import annotations

from fastapi.testclient import TestClient


def test_health_check(client: TestClient) -> None:
    response = client.get("/health")

    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 0
    assert body["message"] == "success"
    assert body["data"]["status"] == "ok"
    assert body["data"]["app"] == "vehicle-api"


def test_swagger_docs_available(client: TestClient) -> None:
    response = client.get("/docs")

    assert response.status_code == 200
    assert "swagger" in response.text.lower()


def test_openapi_schema_contains_required_endpoints(client: TestClient) -> None:
    response = client.get("/openapi.json")

    assert response.status_code == 200
    paths = response.json()["paths"]
    assert set(paths["/api/v1/vehicles/{vehicle_id}/location"]) == {"put"}
    assert set(paths["/api/v1/vehicles/{vehicle_id}/status"]) == {"patch"}
    assert set(paths["/api/v1/vehicles/{vehicle_id}"]) == {"get"}


def test_unknown_route_returns_unified_error(client: TestClient) -> None:
    response = client.get("/api/v1/not-exists")

    assert response.status_code == 404
    body = response.json()
    assert body["code"] == 404
    assert body["data"] is None


def test_method_not_allowed_returns_unified_error(client: TestClient) -> None:
    response = client.delete("/api/v1/vehicles/VH-1001")

    assert response.status_code == 405
    assert response.json()["code"] == 405
