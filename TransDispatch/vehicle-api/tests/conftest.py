"""pytest 公共夹具：每个测试用例使用全新的应用实例，保证内存数据互不干扰。"""

from __future__ import annotations

import sys
from collections.abc import Iterator
from pathlib import Path

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient

PROJECT_ROOT = Path(__file__).resolve().parents[1]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from app.main import create_app  # noqa: E402  (必须在 sys.path 调整之后导入)


@pytest.fixture()
def app() -> FastAPI:
    """全新的应用实例（内存存储也是全新的）。"""

    return create_app()


@pytest.fixture()
def client(app: FastAPI) -> Iterator[TestClient]:
    """FastAPI 测试客户端，退出时自动执行 lifespan。"""

    with TestClient(app) as test_client:
        yield test_client
