package com.example.vehicleapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

/** {@code GET /api/v1/vehicles/{vehicle_id}} 与整体流程的测试。 */
class VehicleQueryTest extends ApiTestSupport {

    @Test
    void getVehicleAfterWrites() {
        String vehicleId = "VH-3003";

        ResponseEntity<JsonNode> locationResponse =
                put(
                        API + "/vehicles/" + vehicleId + "/location",
                        map(
                                "longitude", 113.941452,
                                "latitude", 22.546761,
                                "altitude", 20.0,
                                "speed", 0.0,
                                "heading", 0.0,
                                "accuracy", 8.0,
                                "coordType", "GCJ02",
                                "source", "FUSION",
                                "timestamp", "2026-09-29T05:00:00.000Z",
                                "eventId", "evt-loc-flow"));
        assertEquals(200, locationResponse.getStatusCode().value());

        ResponseEntity<JsonNode> statusResponse =
                patch(
                        API + "/vehicles/" + vehicleId + "/status",
                        map(
                                "ignitionOn", false,
                                "gear", "P",
                                "online", true,
                                "charging", true,
                                "batteryLevel", 66.0,
                                "mileage", 45678.9,
                                "eventId", "evt-status-flow"));
        assertEquals(200, statusResponse.getStatusCode().value());

        ResponseEntity<JsonNode> response = get(API + "/vehicles/" + vehicleId);

        assertEquals(200, response.getStatusCode().value());
        JsonNode body = json(response);
        assertEquals(0, body.get("code").asInt());

        JsonNode data = body.get("data");
        assertEquals(vehicleId, data.get("vehicleId").asText());
        assertEquals(2, data.get("version").asInt());
        assertEquals("GCJ02", data.at("/location/coordType").asText());
        assertEquals("FUSION", data.at("/location/source").asText());
        assertEquals("2026-09-29T05:00:00.000Z", data.at("/location/timestamp").asText());
        assertEquals(66.0, data.at("/status/batteryLevel").asDouble(), 1e-9);
        assertTrue(data.at("/status/charging").asBoolean());
        assertEquals(2, data.at("/status/version").asInt());
        assertTrue(data.get("createdAt").asText().endsWith("Z"));
        assertTrue(data.get("updatedAt").asText().endsWith("Z"));
    }

    @Test
    void getVehicleWithOnlyLocation() {
        String vehicleId = "VH-4004";
        put(
                API + "/vehicles/" + vehicleId + "/location",
                map("longitude", 1.0, "latitude", 2.0));

        JsonNode data = json(get(API + "/vehicles/" + vehicleId)).get("data");

        assertNotNull(data.get("location"));
        assertTrue(data.get("status").isNull());
        assertEquals(1, data.get("version").asInt());
    }

    @Test
    void getVehicleWithOnlyStatus() {
        String vehicleId = "VH-5005";
        patch(API + "/vehicles/" + vehicleId + "/status", map("online", true));

        JsonNode data = json(get(API + "/vehicles/" + vehicleId)).get("data");

        assertTrue(data.get("location").isNull());
        assertFalse(data.get("status").isNull());
        assertEquals(1, data.get("version").asInt());
    }

    @Test
    void vehiclesAreIsolated() {
        patch(API + "/vehicles/VH-A/status", map("online", true, "batteryLevel", 10.0));
        patch(API + "/vehicles/VH-B/status", map("online", false, "batteryLevel", 90.0));

        JsonNode dataA = json(get(API + "/vehicles/VH-A")).get("data");
        JsonNode dataB = json(get(API + "/vehicles/VH-B")).get("data");

        assertEquals(10.0, dataA.at("/status/batteryLevel").asDouble(), 1e-9);
        assertTrue(dataA.at("/status/online").asBoolean());
        assertEquals(90.0, dataB.at("/status/batteryLevel").asDouble(), 1e-9);
        assertFalse(dataB.at("/status/online").asBoolean());
    }
}
