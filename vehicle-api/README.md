# vehicle-api

一个可以直接跑起来、也可以直接提交到 GitHub 的 **车辆位置 / 车辆状态后端接口** 项目。

技术栈：**Java 17+ / Spring Boot 3 / Bean Validation (Jakarta Validation) / Jackson / 内嵌 Tomcat**，存储层使用 **内存存储（进程内 Map）**，属于最小可用版本（MVP）：服务重启后数据丢失，但接口行为、参数校验、乐观锁、统一响应格式、错误码、自动化测试都是完整且可直接复用的。

---

## 1. 项目介绍

### 1.1 它是做什么的

车联网 / 车队管理场景里，车载终端会持续上报两类数据：

1. **位置数据**：经纬度、海拔、速度、航向角、定位精度、坐标系、定位来源等；
2. **车辆状态数据**：点火、挡位、在线、充电、电量、油量、里程、车门、车窗、胎压、故障码等。

本项目把这两类数据的写入与查询封装成了 3 个 REST 接口：位置是**全量覆盖式**写入，状态是**局部更新式**写入（只更新传了的字段），并额外提供车辆信息查询接口。

### 1.2 接口清单

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `PUT` | `/api/v1/vehicles/{vehicle_id}/location` | 设置/更新车辆位置（全量覆盖，upsert） |
| `PATCH` | `/api/v1/vehicles/{vehicle_id}/status` | 设置/更新车辆其他状态（局部更新，upsert） |
| `GET` | `/api/v1/vehicles/{vehicle_id}` | 查询车辆信息（位置 + 状态 + 版本号） |
| `GET` | `/health` | 健康检查（运维辅助接口） |

接口文档：

- Swagger UI：<http://127.0.0.1:8000/docs>
- ReDoc：<http://127.0.0.1:8000/redoc>
- OpenAPI JSON：<http://127.0.0.1:8000/openapi.json>

### 1.3 功能特性

- 统一响应格式：`{"code": 0, "message": "success", "data": ...}`，**成功与失败都走同一套外壳**；
- 基于 Bean Validation 的严格参数校验：范围校验（经纬度、电量、胎压等）、枚举校验（坐标系、定位来源、挡位、开关状态）、未知字段直接拒绝（`FAIL_ON_UNKNOWN_PROPERTIES`，等价于 Pydantic 的 `extra="forbid"`，防止字段拼写错误被静默忽略）；
- `PATCH` 只更新请求中出现过的字段，未出现的字段保持原值；需要清空字段时显式传 `null`；
- `version` 简单乐观锁：`PATCH` 传了 `version` 就必须等于当前版本，否则返回 **409**，并在 `data` 中给出 `currentVersion`；
- 全局异常处理：业务异常、参数校验异常、404/405、未捕获异常都返回统一响应格式；
- 线程安全的内存存储：`HashMap` + `ReentrantLock`，写入与读取都不会出现竞态；
- 开箱可用的自动化测试（JUnit 5 + Spring Boot Test）与 GitHub Actions CI。

### 1.4 技术选型与已知限制

| 项目 | 选型 | 说明 |
| --- | --- | --- |
| Web 框架 | Spring Boot Web (Spring MVC) | 内嵌 Tomcat，提供依赖注入 |
| 数据校验 | Bean Validation (Hibernate Validator) | 声明式校验 + 注解约束 |
| JSON | Jackson | 请求/响应序列化，统一时间格式 |
| 接口文档 | springdoc-openapi | 自动生成 OpenAPI 与 Swagger UI |
| 存储 | 内存（进程内 Map） | **重启即丢数据，多实例之间不共享** |
| 并发控制 | `ReentrantLock` | 单进程内线程安全 |

关于「重启丢数据」：这是刻意为之的最小可用设计。后续要接持久化，只需要把 `InMemoryVehicleStore`
替换成 Redis / MySQL / PostgreSQL 实现，并保持方法签名不变，接口层与测试基本不用改。

---

## 2. 目录结构

```text
vehicle-api/
├── .github/
│   └── workflows/
│       └── test.yml                                # GitHub Actions：mvn test（JDK 17 / 21）
├── .mvn/
│   └── wrapper/
│       ├── maven-wrapper.jar                       # Maven Wrapper（无需预装 Maven）
│       ├── maven-wrapper.properties
│       └── MavenWrapperDownloader.java
├── src/
│   ├── main/
│   │   ├── java/com/example/vehicleapi/
│   │   │   ├── VehicleApiApplication.java          # 应用入口（等价 app/main.py）
│   │   │   ├── api/
│   │   │   │   ├── SystemController.java           # 健康检查 / ReDoc（等价 app/main.py 里的 /health）
│   │   │   │   └── v1/
│   │   │   │       ├── VehicleController.java      # 车辆三个核心接口（等价 app/api/v1/vehicles.py）
│   │   │   │       ├── VehicleIdValidator.java     # 车辆 ID 约束（等价 Path(...) 校验）
│   │   │   │       └── dto/                        # 响应数据模型
│   │   │   ├── core/
│   │   │   │   ├── config/
│   │   │   │   │   ├── Settings.java               # 配置（等价 app/core/config.py）
│   │   │   │   │   ├── JacksonConfig.java          # UTC 时间序列化（等价 app/core/types.py）
│   │   │   │   │   ├── WebConfig.java              # CORS（等价 main.py 里的 CORSMiddleware）
│   │   │   │   │   └── OpenApiConfig.java          # 文档标题/版本/标签
│   │   │   │   ├── error/
│   │   │   │   │   ├── GlobalExceptionHandler.java # 全局异常处理（等价 app/core/errors.py）
│   │   │   │   │   ├── VehicleApiException.java    # 业务异常基类（等价 app/core/exceptions.py）
│   │   │   │   │   ├── VehicleNotFoundException.java
│   │   │   │   │   ├── VersionConflictException.java
│   │   │   │   │   ├── RequestValidationException.java
│   │   │   │   │   ├── ValidationErrorMapper.java  # Bean Validation 错误 → 统一结构
│   │   │   │   │   └── JsonErrorMapper.java        # Jackson 错误 → 统一结构
│   │   │   │   ├── time/
│   │   │   │   │   ├── UtcInstantSerializer.java
│   │   │   │   │   └── UtcInstantDeserializer.java
│   │   │   │   └── web/
│   │   │   │       ├── ApiResponse.java            # 统一响应外壳（等价 app/core/response.py）
│   │   │   │       ├── ErrorCodes.java             # 业务状态码
│   │   │   │       └── FieldError.java             # 逐字段错误
│   │   │   ├── domain/vehicle/
│   │   │   │   ├── CoordType.java                  # 枚举（等价 app/models/vehicle.py）
│   │   │   │   ├── LocationSource.java
│   │   │   │   ├── Gear.java
│   │   │   │   ├── SwitchState.java
│   │   │   │   ├── VehicleKeys.java                # 车门/车窗/胎压允许的键
│   │   │   │   ├── VehicleLocation.java            # 位置模型（等价 app/schemas/vehicle.py）
│   │   │   │   ├── VehicleStatusPatch.java         # 状态局部更新模型
│   │   │   │   ├── VehicleStatus.java              # 落库后的完整状态
│   │   │   │   ├── AllowedStatusKeys.java          # 自定义约束注解
│   │   │   │   └── AllowedStatusKeysValidator.java
│   │   │   └── service/
│   │   │       ├── InMemoryVehicleStore.java       # 内存存储 + 乐观锁（等价 app/services/store.py）
│   │   │       └── VehicleRecord.java
│   │   └── resources/
│   │       └── application.yml                     # 配置项与默认值
│   └── test/java/com/example/vehicleapi/
│       ├── ApiTestSupport.java                     # 测试基类（等价 tests/conftest.py）
│       ├── HealthAndDocsTest.java                  # 等价 tests/test_health_and_docs.py
│       ├── LocationApiTest.java                    # 等价 tests/test_location.py
│       ├── StatusApiTest.java                      # 等价 tests/test_status.py
│       └── VehicleQueryTest.java                   # 等价 tests/test_vehicle_query.py
├── .gitignore
├── LICENSE
├── README.md
├── mvnw                                            # Maven Wrapper（Linux / macOS）
├── mvnw.cmd                                        # Maven Wrapper（Windows）
└── pom.xml                                         # Maven 构建配置（等价 requirements.txt）
```

---

## 3. 安装依赖

### 3.1 环境要求

- **JDK 17 或 21（LTS）**（Spring Boot 3 的官方支持范围是 JDK 17 ~ 23）
- 无需预装 Maven：项目自带 **Maven Wrapper**（`mvnw` / `mvnw.cmd`），首次运行会自动下载 Maven 与依赖（需要联网）

检查 JDK：

```bash
java -version
```

### 3.2 首次构建（会自动下载依赖）

Windows（PowerShell）：

```powershell
cd vehicle-api
.\mvnw.cmd test
```

macOS / Linux：

```bash
cd vehicle-api
./mvnw test
```

如果你已经安装了 Maven，也可以直接使用 `mvn`：

```bash
mvn test
```

---

## 4. 本地运行

### 4.1 启动服务

在项目根目录（`vehicle-api/`，也就是能看到 `pom.xml` 这一层）执行：

Windows：

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux：

```bash
./mvnw spring-boot:run
```

或者先打包再运行（会读取 `HOST` / `PORT` / `DEBUG` 等环境变量）：

```bash
./mvnw -q package
java -jar target/vehicle-api-1.0.0.jar
```

启动成功后：

- 接口文档：<http://127.0.0.1:8000/docs>
- 健康检查：<http://127.0.0.1:8000/health>

### 4.2 可选配置

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `APP_NAME` | `vehicle-api` | 应用名称（显示在文档标题） |
| `APP_VERSION` | `1.0.0` | 应用版本 |
| `API_PREFIX` | `/api/v1` | 接口前缀 |
| `HOST` | `127.0.0.1` | 监听地址 |
| `PORT` | `8000` | 监听端口 |
| `DEBUG` | `false` | 为 `true` 时输出调试日志 |

PowerShell 示例：

```powershell
$env:PORT = "9000"
.\mvnw.cmd spring-boot:run
```

---

## 5. 统一响应格式

所有接口（**包括错误响应**）都返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

- `code`：业务状态码，`0` 表示成功，非 0 表示失败；
- `message`：提示信息；
- `data`：业务数据；出错时为错误详情或 `null`。

### 5.1 业务状态码

| code | HTTP 状态码 | 含义 |
| --- | --- | --- |
| `0` | 200 | 成功 |
| `40001` | 422 | 请求参数校验失败（`data.errors` 为逐字段错误列表） |
| `40401` | 404 | 车辆不存在 |
| `40901` | 409 | 乐观锁版本冲突（`data.currentVersion` 为服务端最新版本） |
| `50000` | 500 | 服务器内部错误 |
| 其他 | 4xx/5xx | 框架级错误（如 404/405），`code` 与 HTTP 状态码一致 |

### 5.2 参数校验失败示例

```json
{
  "code": 40001,
  "message": "请求参数校验失败",
  "data": {
    "errors": [
      {
        "field": "body.latitude",
        "type": "less_than_equal",
        "message": "Input should be less than or equal to 90"
      }
    ]
  }
}
```

> `field` 的取值与 Python 版本完全一致（例如 `body.latitude`、`path.vehicle_id`）；
> `type` 采用 Pydantic 的错误类型命名，仅在个别约束上做等价映射。

---

## 6. 字段说明

### 6.1 位置字段（PUT /location）

| 字段 | 类型 | 必填 | 约束 | 说明 |
| --- | --- | --- | --- | --- |
| `longitude` | number | 是 | `-180 ~ 180` | 经度 |
| `latitude` | number | 是 | `-90 ~ 90` | 纬度 |
| `altitude` | number / null | 否 | `-500 ~ 10000` | 海拔（米） |
| `speed` | number / null | 否 | `0 ~ 1000` | 速度（km/h） |
| `heading` | number / null | 否 | `0 ~ 360` | 航向角（度，正北为 0，顺时针） |
| `accuracy` | number / null | 否 | `0 ~ 100000` | 定位精度（米） |
| `coordType` | string | 否 | `WGS84` / `GCJ02` / `BD09` | 坐标系，默认 `WGS84` |
| `source` | string | 否 | `GPS` / `BEIDOU` / `GLONASS` / `LBS` / `WIFI` / `FUSION` / `MANUAL` / `UNKNOWN` | 定位来源，默认 `UNKNOWN` |
| `timestamp` | string | 否 | ISO 8601 | 采集时间，不传则取服务器当前时间，返回统一为 UTC `Z` 结尾 |
| `eventId` | string / null | 否 | 长度 `1 ~ 64` | 事件 ID，用于链路追踪与幂等对账 |

> 位置接口是**全量覆盖**：请求体里没传的可选字段会被重置为 `null`（或默认值），不会保留上一次的值。

### 6.2 状态字段（PATCH /status）

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| `ignitionOn` | boolean | - | 点火状态 |
| `gear` | string | `P` / `R` / `N` / `D` / `S` / `L` | 挡位 |
| `online` | boolean | - | 是否在线 |
| `charging` | boolean | - | 是否充电中 |
| `batteryLevel` | number | `0 ~ 100` | 动力电池电量（%） |
| `fuelLevel` | number | `0 ~ 100` | 燃油余量（%） |
| `mileage` | number | `≥ 0` | 总里程（km） |
| `doors` | object | 键：`frontLeft` / `frontRight` / `rearLeft` / `rearRight` / `trunk`，值：`OPEN` / `CLOSED` | 车门状态 |
| `windows` | object | 键：`frontLeft` / `frontRight` / `rearLeft` / `rearRight`，值：`OPEN` / `CLOSED` | 车窗状态 |
| `tirePressure` | object | 键：`frontLeft` / `frontRight` / `rearLeft` / `rearRight` / `spare`，值：`0 ~ 1000`（kPa） | 胎压 |
| `faultCodes` | array[string] | 最多 100 项 | 故障码列表，传 `[]` 表示清除 |
| `timestamp` | string | ISO 8601 | 状态采集时间，不传则保持原值（首次创建取服务器当前时间） |
| `version` | integer | `≥ 0` | **乐观锁**：传了就必须等于当前版本，否则 409；不传则不校验 |
| `eventId` | string / null | 长度 `1 ~ 64` | 事件 ID |
| `extra` | object / null | - | 扩展字段，可放厂商私有数据 |

> 状态接口是**局部更新**：只有出现在请求体里的字段才会被写入，其余字段保持原值。

---

## 7. 接口测试示例

下面的示例都假设服务运行在 `http://127.0.0.1:8000`，车辆 ID 使用 `VH-1001`。

### 7.1 设置/更新车辆位置

```bash
curl -X PUT "http://127.0.0.1:8000/api/v1/vehicles/VH-1001/location" \
  -H "Content-Type: application/json" \
  -d '{
    "longitude": 116.397128,
    "latitude": 39.916527,
    "altitude": 45.5,
    "speed": 32.4,
    "heading": 88.0,
    "accuracy": 5.0,
    "coordType": "WGS84",
    "source": "GPS",
    "timestamp": "2026-09-29T02:15:30.000Z",
    "eventId": "evt-location-1"
  }'
```

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "vehicleId": "VH-1001",
    "version": 1,
    "location": {
      "longitude": 116.397128,
      "latitude": 39.916527,
      "altitude": 45.5,
      "speed": 32.4,
      "heading": 88.0,
      "accuracy": 5.0,
      "coordType": "WGS84",
      "source": "GPS",
      "timestamp": "2026-09-29T02:15:30.000Z",
      "eventId": "evt-location-1"
    },
    "updatedAt": "2026-09-29T06:12:04.512Z",
    "created": true
  }
}
```

PowerShell 版本：

```powershell
$body = @{
  longitude = 116.397128
  latitude  = 39.916527
  speed     = 32.4
  heading   = 88.0
  accuracy  = 5.0
  coordType = "WGS84"
  source    = "GPS"
  eventId   = "evt-location-1"
} | ConvertTo-Json

Invoke-RestMethod -Method Put `
  -Uri "http://127.0.0.1:8000/api/v1/vehicles/VH-1001/location" `
  -ContentType "application/json" `
  -Body $body
```

### 7.2 设置/更新车辆状态（局部更新）

第一次写入：只传电量与在线状态，其余字段保持 `null`。

```bash
curl -X PATCH "http://127.0.0.1:8000/api/v1/vehicles/VH-1001/status" \
  -H "Content-Type: application/json" \
  -d '{
    "ignitionOn": true,
    "gear": "D",
    "online": true,
    "batteryLevel": 82.5,
    "doors": { "frontLeft": "CLOSED", "frontRight": "CLOSED" },
    "faultCodes": ["P0301"],
    "eventId": "evt-status-1"
  }'
```

第二次写入：只更新 `batteryLevel`，其他字段（`gear`、`doors`、`faultCodes` …）保持不变，并带上乐观锁版本号。
（此时服务端版本已经是 `2`：位置写入 1 次 + 状态写入 1 次。）

```bash
curl -X PATCH "http://127.0.0.1:8000/api/v1/vehicles/VH-1001/status" \
  -H "Content-Type: application/json" \
  -d '{
    "batteryLevel": 77.5,
    "version": 2
  }'
```

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "vehicleId": "VH-1001",
    "version": 3,
    "status": {
      "ignitionOn": true,
      "gear": "D",
      "online": true,
      "charging": null,
      "batteryLevel": 77.5,
      "fuelLevel": null,
      "mileage": null,
      "doors": { "frontLeft": "CLOSED", "frontRight": "CLOSED" },
      "windows": null,
      "tirePressure": null,
      "faultCodes": ["P0301"],
      "timestamp": "2026-09-29T06:12:04.512Z",
      "version": 3,
      "eventId": "evt-status-1",
      "extra": null
    },
    "updatedAt": "2026-09-29T06:13:10.884Z",
    "created": false
  }
}
```

### 7.3 乐观锁冲突（409）

用旧版本号写入：

```bash
curl -X PATCH "http://127.0.0.1:8000/api/v1/vehicles/VH-1001/status" \
  -H "Content-Type: application/json" \
  -d '{"batteryLevel": 10.0, "version": 2}'
```

响应（HTTP 409）：

```json
{
  "code": 40901,
  "message": "版本冲突：请求携带的 version 不是当前最新版本，请重新查询车辆信息后重试",
  "data": {
    "currentVersion": 3,
    "providedVersion": 2
  }
}
```

### 7.4 查询车辆信息

```bash
curl "http://127.0.0.1:8000/api/v1/vehicles/VH-1001"
```

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "vehicleId": "VH-1001",
    "version": 3,
    "location": {
      "longitude": 116.397128,
      "latitude": 39.916527,
      "altitude": 45.5,
      "speed": 32.4,
      "heading": 88.0,
      "accuracy": 5.0,
      "coordType": "WGS84",
      "source": "GPS",
      "timestamp": "2026-09-29T02:15:30.000Z",
      "eventId": "evt-location-1"
    },
    "status": {
      "ignitionOn": true,
      "gear": "D",
      "online": true,
      "charging": null,
      "batteryLevel": 77.5,
      "fuelLevel": null,
      "mileage": null,
      "doors": { "frontLeft": "CLOSED", "frontRight": "CLOSED" },
      "windows": null,
      "tirePressure": null,
      "faultCodes": ["P0301"],
      "timestamp": "2026-09-29T06:12:04.512Z",
      "version": 3,
      "eventId": "evt-status-1",
      "extra": null
    },
    "createdAt": "2026-09-29T06:12:04.512Z",
    "updatedAt": "2026-09-29T06:13:10.884Z"
  }
}
```

### 7.5 查询不存在的车辆（404）

```bash
curl "http://127.0.0.1:8000/api/v1/vehicles/NOT-EXIST"
```

```json
{
  "code": 40401,
  "message": "车辆不存在：NOT-EXIST",
  "data": { "vehicleId": "NOT-EXIST" }
}
```

### 7.6 参数校验失败（422）

```bash
curl -X PUT "http://127.0.0.1:8000/api/v1/vehicles/VH-1001/location" \
  -H "Content-Type: application/json" \
  -d '{"longitude": 116.397128, "latitude": 999}'
```

```json
{
  "code": 40001,
  "message": "请求参数校验失败",
  "data": {
    "errors": [
      {
        "field": "body.latitude",
        "type": "less_than_equal",
        "message": "Input should be less than or equal to 90"
      }
    ]
  }
}
```

### 7.7 在 /docs 里点着测

1. 打开 <http://127.0.0.1:8000/docs>；
2. 展开 `PUT /api/v1/vehicles/{vehicle_id}/location`，点击 **Try it out**；
3. 填入 `vehicle_id`（例如 `VH-1001`）与请求体，点击 **Execute**；
4. 状态接口 `PATCH /api/v1/vehicles/{vehicle_id}/status` 同理，可以看到只更新传入字段的效果。

---

## 8. 运行测试

```bash
./mvnw test
```

Windows：

```powershell
.\mvnw.cmd test
```

常用参数：

```bash
./mvnw test -Dtest=StatusApiTest          # 只跑状态接口测试
./mvnw test -Dtest='*ApiTest'             # 按类名模式筛选
./mvnw -q test -DtrimStackTrace=false     # 输出完整堆栈便于排查
```

测试覆盖点（节选）：

- 位置接口的创建、覆盖更新、默认时间、可选字段缺省；
- 状态接口的局部更新语义（未传字段保持不变）、显式 `null` 清空；
- 乐观锁：版本匹配成功、版本落后返回 409、版本超前返回 409、不传版本不校验；
- 参数校验：经纬度/电量/里程/胎压范围、枚举取值、未知字段、非法车辆 ID；
- 统一响应格式：健康检查、404、405、文档与 OpenAPI 路径。

---

## 9. 如何上传到 GitHub

### 9.1 方式一：命令行（推荐）

1）在 GitHub 网页上新建一个空仓库，例如 `vehicle-api`。
**不要**勾选 "Add a README file"、"Add .gitignore"、"Choose a license"，保持空仓库，避免推送冲突。

2）在本地项目目录里初始化 git 并提交（把 `<你的用户名>` 换成你的 GitHub 用户名）：

```bash
cd vehicle-api
git init
git branch -M main
git add .
git commit -m "feat: 初始化 vehicle-api（车辆位置/状态接口）"
git remote add origin https://github.com/<你的用户名>/vehicle-api.git
git push -u origin main
```

3）如果推送时提示需要登录，推荐使用 **Personal Access Token**（Settings → Developer settings → Personal access tokens）或配置 SSH：

```bash
# 使用 SSH 的方式
git remote set-url origin git@github.com:<你的用户名>/vehicle-api.git
git push -u origin main
```

### 9.2 方式二：先克隆空仓库再拷贝文件

```bash
git clone https://github.com/<你的用户名>/vehicle-api.git
# 把本项目里的文件（不含 .git 目录）复制到 clone 下来的目录
cd vehicle-api
git add .
git commit -m "feat: 初始化 vehicle-api"
git push origin main
```

### 9.3 后续更新

```bash
git status
git add .
git commit -m "docs: 更新 README 接口示例"
git push
```

### 9.4 推送前的检查清单

- [ ] 确认没有把 `target/`、`.idea/`、`.env` 提交上去（`.gitignore` 已覆盖）；
- [ ] 本地 `./mvnw test` 全部通过；
- [ ] `README.md` 里的接口示例与实际行为一致；
- [ ] 推送到 GitHub 后，仓库的 **Actions** 页面出现 `test` 工作流并显示绿色通过。

### 9.5 GitHub Actions

`.github/workflows/test.yml` 会在以下时机自动运行：推送到 `main`/`master`、提交 Pull Request、手动触发。
它会在 JDK 17 / 21 两个版本上分别执行 `mvn -B -q test`。

---

## 10. 设计说明

### 10.1 为什么用统一响应外壳

HTTP 状态码表达「传输层语义」，业务 `code` 表达「业务语义」。两者结合后：

- 网关/前端只需要判断 `code == 0`；
- 监控系统仍然可以按 HTTP 状态码做告警与统计；
- 出错时 `data` 里能带上结构化上下文（例如 `currentVersion`）。

### 10.2 乐观锁是怎么工作的

- 车辆记录上有一个自增的 `version`，**每一次成功写入（位置或状态）都会 +1**；
- `PATCH /status` 的请求体可以带 `version`：带上时必须与当前版本相等，否则返回 409；
- 写入成功后的最新版本号在响应 `data.version` 中返回；
- `GET /vehicles/{vehicle_id}` 也会返回当前版本号，客户端可据此重试。

典型用法（读 → 改 → 写）：

```text
GET  /api/v1/vehicles/VH-1001        -> version = 2
PATCH /api/v1/vehicles/VH-1001/status（携带 version = 2） -> 成功，version = 3
```

如果此时另一个客户端已经把它改到了 `version = 3`，那么携带 `version = 2` 的请求会返回 409，客户端重新拉取数据再试即可。

### 10.3 局部更新是怎么实现的

Python 版本用 Pydantic 的 `model_fields_set` 区分「字段没传」和「字段显式传了 `null`」。
Java 版本用同样的语义：`VehicleStatusPatch` 的每个 setter 都会把字段名记录到 `providedFields`，
Jackson 只有在 JSON 里真正出现该字段时才会调用对应的 setter，因此：

- 字段没出现 → 保持原值；
- 字段出现且为 `null` → 显式清空。

### 10.4 并发与线程安全

内存仓库用 `ReentrantLock` 保护，「校验版本 + 合并字段 + 自增版本」这三步在同一个锁内完成，
因此不会出现「两个请求都以为自己是最新版本」的竞态。读取接口返回的是副本，
调用方无法误改仓库中的原始对象。

### 10.5 后续可以怎么演进

1. **持久化**：新增 `RedisVehicleStore` 或基于 Spring Data JPA 的实现，保持与 `InMemoryVehicleStore` 相同的方法签名；
2. **批量上报**：增加 `POST /api/v1/vehicles/{vehicle_id}/events` 接收数组，内部复用同一套校验模型；
3. **鉴权**：接入 API Key / JWT（Spring Security）；
4. **历史轨迹**：把位置写入时序库（如 TimescaleDB / InfluxDB），并增加按时间范围查询的接口；
5. **可观测性**：增加请求 ID、结构化日志、Micrometer / Prometheus 指标。

---

## 11. 许可证

本项目使用 [MIT License](LICENSE)。
