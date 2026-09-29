package com.example.vehicleapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.ResponseEntity;

/** {@code PUT /api/v1/vehicles/{vehicle_id}/location} 的测试。 */
class LocationApiTest extends ApiTestSupport {

    private static final String VEHICLE_ID = "VH-1001";
    private static final String LOCATION_URL = API + "/vehicles/" + VEHICLE_ID + "/location";
    private static final String QUERY_URL = API + "/vehicles/" + VEHICLE_ID;

    private static Map<String, Object> buildLocation() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("longitude", 116.397128);
        payload.put("latitude", 39.916527);
        payload.put("altitude", 45.5);
        payload.put("speed", 32.4);
        payload.put("heading", 88.0);
        payload.put("accuracy", 5.0);
        payload.put("coordType", "WGS84");
        payload.put("source", "GPS");
        payload.put("timestamp", "2026-09-29T02:15:30.000Z");
        payload.put("eventId", "evt-location-1");
        return payload;
    }

    @Test
    void putLocationCreatesVehicle() {
        ResponseEntity<JsonNode> response = put(LOCATION_URL, buildLocation());

        assertEquals(200, response.getStatusCode().value());
        JsonNode body = json(response);
        assertEquals(0, body.get("code").asInt());
        assertEquals("success", body.get("message").asText());

        JsonNode data = body.get("data");
        assertEquals(VEHICLE_ID, data.get("vehicleId").asText());
        assertEquals(1, data.get("version").asInt());
        assertTrue(data.get("created").asBoolean());
        assertTrue(data.get("updatedAt").asText().endsWith("Z"));

        JsonNode location = data.get("location");
        assertEquals(116.397128, location.get("longitude").asDouble(), 1e-9);
        assertEquals(39.916527, location.get("latitude").asDouble(), 1e-9);
        assertEquals(45.5, location.get("altitude").asDouble(), 1e-9);
        assertEquals(32.4, location.get("speed").asDouble(), 1e-9);
        assertEquals(88.0, location.get("heading").asDouble(), 1e-9);
        assertEquals(5.0, location.get("accuracy").asDouble(), 1e-9);
        assertEquals("WGS84", location.get("coordType").asText());
        assertEquals("GPS", location.get("source").asText());
        assertEquals("2026-09-29T02:15:30.000Z", location.get("timestamp").asText());
        assertEquals("evt-location-1", location.get("eventId").asText());
    }

    @Test
    void putLocationOverwritesPreviousLocation() {
        assertEquals(200, put(LOCATION_URL, buildLocation()).getStatusCode().value());

        Map<String, Object> second = buildLocation();
        second.put("longitude", 121.473701);
        second.put("latitude", 31.230416);
        second.put("speed", 0.0);
        second.put("eventId", "evt-location-2");
        second.put("timestamp", "2026-09-29T03:00:00.000Z");
        // 这次不传 altitude / heading，用于验证位置接口是「全量覆盖」而不是「局部更新」
        second.remove("altitude");
        second.remove("heading");

        ResponseEntity<JsonNode> response = put(LOCATION_URL, second);

        assertEquals(200, response.getStatusCode().value());
        JsonNode data = json(response).get("data");
        assertFalse(data.get("created").asBoolean());
        assertEquals(2, data.get("version").asInt());
        assertEquals(121.473701, data.at("/location/longitude").asDouble(), 1e-9);
        assertEquals("evt-location-2", data.at("/location/eventId").asText());

        // 位置是整体覆盖：未传的字段回到默认值，不会保留上一份数据
        assertTrue(data.at("/location/altitude").isNull());
        assertTrue(data.at("/location/heading").isNull());

        JsonNode detail = json(get(QUERY_URL)).get("data");
        assertEquals(2, detail.get("version").asInt());
        assertEquals(121.473701, detail.at("/location/longitude").asDouble(), 1e-9);
    }

    @Test
    void putLocationTimestampDefaultsToServerTime() {
        Map<String, Object> payload = buildLocation();
        payload.remove("timestamp");

        ResponseEntity<JsonNode> response = put(LOCATION_URL, payload);

        assertEquals(200, response.getStatusCode().value());
        String timestamp = json(response).at("/data/location/timestamp").asText();
        assertTrue(timestamp.endsWith("Z"));
        assertEquals("2026-09-29T02:15:30.000Z".length(), timestamp.length());
    }

    @Test
    void putLocationWithoutOptionalFields() {
        ResponseEntity<JsonNode> response =
                put(LOCATION_URL, map("longitude", 116.397128, "latitude", 39.916527));

        assertEquals(200, response.getStatusCode().value());
        JsonNode location = json(response).at("/data/location");
        assertEquals("WGS84", location.get("coordType").asText());
        assertEquals("UNKNOWN", location.get("source").asText());
        assertTrue(location.get("eventId").isNull());
        assertTrue(location.get("speed").isNull());
    }

    static Stream<Arguments> invalidLocationValues() {
        return Stream.of(
                arguments("longitude", 200.0, "body.longitude"),
                arguments("latitude", -91.0, "body.latitude"),
                arguments("heading", 400.0, "body.heading"),
                arguments("speed", -1.0, "body.speed"),
                arguments("coordType", "WGS84-X", "body.coordType"),
                arguments("source", "MAGIC", "body.source"),
                arguments("eventId", "", "body.eventId"));
    }

    @ParameterizedTest(name = "{0} = {1}")
    @MethodSource("invalidLocationValues")
    void putLocationRejectsInvalidValues(String field, Object value, String expectedField) {
        Map<String, Object> payload = buildLocation();
        payload.put(field, value);

        ResponseEntity<JsonNode> response = put(LOCATION_URL, payload);

        assertEquals(422, response.getStatusCode().value());
        JsonNode body = json(response);
        assertEquals(40001, body.get("code").asInt());
        assertEquals("请求参数校验失败", body.get("message").asText());
        assertTrue(
                errorFields(body).contains(expectedField),
                () -> "errors=" + errorFields(body));
    }

    @Test
    void putLocationRejectsMissingRequiredFields() {
        ResponseEntity<JsonNode> response = put(LOCATION_URL, map("longitude", 116.397128));

        assertEquals(422, response.getStatusCode().value());
        assertTrue(errorFields(json(response)).contains("body.latitude"));
    }

    @Test
    void putLocationRejectsUnknownFields() {
        Map<String, Object> payload = buildLocation();
        payload.put("lontitude", 1.0);

        ResponseEntity<JsonNode> response = put(LOCATION_URL, payload);

        assertEquals(422, response.getStatusCode().value());
        assertTrue(errorFields(json(response)).contains("body.lontitude"));
    }

    @Test
    void putLocationRejectsInvalidVehicleId() {
        ResponseEntity<JsonNode> response =
                put(API + "/vehicles/" + VEHICLE_ID + " BAD/location", buildLocation());

        assertEquals(422, response.getStatusCode().value());
        assertTrue(errorFields(json(response)).contains("path.vehicle_id"));
    }

    @Test
    void getUnknownVehicleReturns404() {
        ResponseEntity<JsonNode> response = get(API + "/vehicles/NOT-EXIST");

        assertEquals(404, response.getStatusCode().value());
        assertEquals(
                tree(
                        """
                        {"code":40401,"message":"车辆不存在：NOT-EXIST","data":{"vehicleId":"NOT-EXIST"}}
                        """),
                json(response));
    }
}
