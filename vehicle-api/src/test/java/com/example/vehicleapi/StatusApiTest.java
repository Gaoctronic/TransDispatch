package com.example.vehicleapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.ResponseEntity;

/** {@code PATCH /api/v1/vehicles/{vehicle_id}/status} 的测试。 */
class StatusApiTest extends ApiTestSupport {

    private static final String VEHICLE_ID = "VH-2002";
    private static final String STATUS_URL = API + "/vehicles/" + VEHICLE_ID + "/status";
    private static final String QUERY_URL = API + "/vehicles/" + VEHICLE_ID;

    @Test
    void patchCreatesVehicleWithOnlyProvidedFields() {
        ResponseEntity<JsonNode> response = patch(STATUS_URL, map("batteryLevel", 82.5, "online", true));

        assertEquals(200, response.getStatusCode().value());
        JsonNode body = json(response);
        assertEquals(0, body.get("code").asInt());

        JsonNode data = body.get("data");
        assertEquals(VEHICLE_ID, data.get("vehicleId").asText());
        assertTrue(data.get("created").asBoolean());
        assertEquals(1, data.get("version").asInt());

        JsonNode status = data.get("status");
        assertEquals(82.5, status.get("batteryLevel").asDouble(), 1e-9);
        assertTrue(status.get("online").asBoolean());
        // 未传入的字段保持为 null，而不是被写成默认值
        assertTrue(status.get("ignitionOn").isNull());
        assertTrue(status.get("gear").isNull());
        assertTrue(status.get("charging").isNull());
        assertTrue(status.get("fuelLevel").isNull());
        assertTrue(status.get("mileage").isNull());
        assertTrue(status.get("doors").isNull());
        assertTrue(status.get("windows").isNull());
        assertTrue(status.get("tirePressure").isNull());
        assertTrue(status.get("faultCodes").isNull());
        assertTrue(status.get("eventId").isNull());
        assertTrue(status.get("extra").isNull());
    }

    @Test
    void patchOnlyUpdatesProvidedFields() {
        patch(
                STATUS_URL,
                map(
                        "ignitionOn", true,
                        "gear", "D",
                        "online", true,
                        "batteryLevel", 80.0,
                        "fuelLevel", 35.0,
                        "mileage", 12000.5,
                        "doors", map("frontLeft", "CLOSED", "frontRight", "CLOSED"),
                        "windows", map("frontLeft", "OPEN"),
                        "tirePressure", map("frontLeft", 235.0, "frontRight", 236.5),
                        "faultCodes", List.of("P0301"),
                        "eventId", "evt-status-1",
                        "extra", map("vendor", "acme")));

        ResponseEntity<JsonNode> response = patch(STATUS_URL, map("online", false, "batteryLevel", 77.5));

        assertEquals(200, response.getStatusCode().value());
        JsonNode data = json(response).get("data");
        assertEquals(2, data.get("version").asInt());

        JsonNode status = data.get("status");
        // 本次传入的字段被更新
        assertFalse(status.get("online").asBoolean());
        assertEquals(77.5, status.get("batteryLevel").asDouble(), 1e-9);
        // 本次未传入的字段保持原值
        assertTrue(status.get("ignitionOn").asBoolean());
        assertEquals("D", status.get("gear").asText());
        assertEquals(35.0, status.get("fuelLevel").asDouble(), 1e-9);
        assertEquals(12000.5, status.get("mileage").asDouble(), 1e-9);
        assertEquals(
                tree("{\"frontLeft\":\"CLOSED\",\"frontRight\":\"CLOSED\"}"), status.get("doors"));
        assertEquals(tree("{\"frontLeft\":\"OPEN\"}"), status.get("windows"));
        assertEquals(
                tree("{\"frontLeft\":235.0,\"frontRight\":236.5}"), status.get("tirePressure"));
        assertEquals(tree("[\"P0301\"]"), status.get("faultCodes"));
        assertEquals("evt-status-1", status.get("eventId").asText());
        assertEquals(tree("{\"vendor\":\"acme\"}"), status.get("extra"));
    }

    @Test
    void patchCanClearFieldWithExplicitNull() {
        patch(STATUS_URL, map("faultCodes", List.of("P0301"), "doors", map("trunk", "OPEN")));

        ResponseEntity<JsonNode> response = patch(STATUS_URL, map("faultCodes", null, "doors", null));

        assertEquals(200, response.getStatusCode().value());
        JsonNode status = json(response).at("/data/status");
        assertTrue(status.get("faultCodes").isNull());
        assertTrue(status.get("doors").isNull());
    }

    @Test
    void patchStatusWithAllFields() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ignitionOn", true);
        payload.put("gear", "P");
        payload.put("online", true);
        payload.put("charging", false);
        payload.put("batteryLevel", 100.0);
        payload.put("fuelLevel", 0.0);
        payload.put("mileage", 888.8);
        payload.put(
                "doors",
                map(
                        "frontLeft", "CLOSED",
                        "frontRight", "CLOSED",
                        "rearLeft", "CLOSED",
                        "rearRight", "CLOSED",
                        "trunk", "CLOSED"));
        payload.put(
                "windows",
                map(
                        "frontLeft", "CLOSED",
                        "frontRight", "CLOSED",
                        "rearLeft", "CLOSED",
                        "rearRight", "CLOSED"));
        payload.put(
                "tirePressure",
                map(
                        "frontLeft", 240.0,
                        "frontRight", 241.0,
                        "rearLeft", 239.5,
                        "rearRight", 240.5,
                        "spare", 250.0));
        payload.put("faultCodes", List.of());
        payload.put("timestamp", "2026-09-29T04:20:00.000Z");
        payload.put("version", 0);
        payload.put("eventId", "evt-status-full");
        payload.put("extra", map("odometer", 888.8, "tags", List.of("test")));

        ResponseEntity<JsonNode> response = patch(STATUS_URL, payload);

        assertEquals(200, response.getStatusCode().value());
        JsonNode status = json(response).at("/data/status");
        assertEquals("2026-09-29T04:20:00.000Z", status.get("timestamp").asText());
        assertEquals(1, status.get("version").asInt());
        assertEquals("P", status.get("gear").asText());
        assertEquals("CLOSED", status.at("/doors/trunk").asText());
        assertEquals(250.0, status.at("/tirePressure/spare").asDouble(), 1e-9);
        assertEquals(tree("[]"), status.get("faultCodes"));
        assertEquals(tree("{\"odometer\":888.8,\"tags\":[\"test\"]}"), status.get("extra"));
    }

    @Test
    void patchStatusWithoutVersionSkipsOptimisticLock() {
        patch(STATUS_URL, map("online", true));
        patch(STATUS_URL, map("online", false));

        ResponseEntity<JsonNode> response = patch(STATUS_URL, map("online", true));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(3, json(response).at("/data/version").asInt());
    }

    @Test
    void patchStatusWithMatchingVersionSucceeds() {
        ResponseEntity<JsonNode> first = patch(STATUS_URL, map("online", true));
        assertEquals(1, json(first).at("/data/version").asInt());

        ResponseEntity<JsonNode> second = patch(STATUS_URL, map("version", 1, "batteryLevel", 60.0));

        assertEquals(200, second.getStatusCode().value());
        assertEquals(2, json(second).at("/data/version").asInt());
    }

    @Test
    void patchStatusWithStaleVersionReturns409() {
        patch(STATUS_URL, map("online", true)); // version -> 1
        patch(STATUS_URL, map("batteryLevel", 90.0)); // version -> 2

        ResponseEntity<JsonNode> response = patch(STATUS_URL, map("version", 1, "batteryLevel", 10.0));

        assertEquals(409, response.getStatusCode().value());
        assertEquals(
                tree(
                        """
                        {"code":40901,
                         "message":"版本冲突：请求携带的 version 不是当前最新版本，请重新查询车辆信息后重试",
                         "data":{"currentVersion":2,"providedVersion":1}}
                        """),
                json(response));

        // 冲突后数据没有被修改
        JsonNode detail = json(get(QUERY_URL)).get("data");
        assertEquals(2, detail.get("version").asInt());
        assertEquals(90.0, detail.at("/status/batteryLevel").asDouble(), 1e-9);
    }

    @Test
    void patchStatusWithFutureVersionReturns409() {
        patch(STATUS_URL, map("online", true)); // version -> 1

        ResponseEntity<JsonNode> response = patch(STATUS_URL, map("version", 99, "online", false));

        assertEquals(409, response.getStatusCode().value());
        assertEquals(1, json(response).at("/data/currentVersion").asInt());
        assertEquals(99, json(response).at("/data/providedVersion").asInt());
    }

    static Stream<Arguments> invalidStatusPayloads() {
        return Stream.of(
                arguments(map("batteryLevel", 150.0), "body.batteryLevel"),
                arguments(map("batteryLevel", -0.1), "body.batteryLevel"),
                arguments(map("fuelLevel", 101.0), "body.fuelLevel"),
                arguments(map("mileage", -5.0), "body.mileage"),
                arguments(map("gear", "X"), "body.gear"),
                arguments(map("version", -1), "body.version"),
                arguments(map("doors", map("front", "OPEN")), "body.doors"),
                arguments(map("windows", map("middle", "CLOSED")), "body.windows"),
                arguments(map("tirePressure", map("frontLeft", 2000.0)), "body.tirePressure"),
                arguments(map("faultCodes", "P0301"), "body.faultCodes"),
                arguments(map("unknownField", 1), "body.unknownField"));
    }

    @ParameterizedTest(name = "invalid status payload case {index}")
    @MethodSource("invalidStatusPayloads")
    void patchStatusRejectsInvalidPayload(Map<String, Object> payload, String expectedField) {
        ResponseEntity<JsonNode> response = patch(STATUS_URL, payload);

        assertEquals(422, response.getStatusCode().value());
        JsonNode body = json(response);
        assertEquals(40001, body.get("code").asInt());
        assertTrue(
                errorFields(body).contains(expectedField),
                () -> "errors=" + errorFields(body));
    }

    @Test
    void patchStatusAcceptsEmptyBody() {
        ResponseEntity<JsonNode> response = patch(STATUS_URL, new LinkedHashMap<String, Object>());

        assertEquals(200, response.getStatusCode().value());
        JsonNode status = json(response).at("/data/status");
        assertEquals(1, status.get("version").asInt());
        assertTrue(status.get("online").isNull());
    }

    @Test
    void patchStatusRejectsNonJsonBody() {
        ResponseEntity<JsonNode> response = patchRaw(STATUS_URL, "online=true");

        assertEquals(422, response.getStatusCode().value());
        assertEquals(40001, json(response).get("code").asInt());
    }
}
