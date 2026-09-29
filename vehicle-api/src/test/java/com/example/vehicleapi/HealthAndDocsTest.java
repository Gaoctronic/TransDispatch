package com.example.vehicleapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

/** 健康检查、接口文档与统一响应格式的测试。 */
class HealthAndDocsTest extends ApiTestSupport {

    @Test
    void healthCheck() {
        ResponseEntity<JsonNode> response = get("/health");

        assertEquals(200, response.getStatusCode().value());
        JsonNode body = json(response);
        assertEquals(0, body.get("code").asInt());
        assertEquals("success", body.get("message").asText());
        assertEquals("ok", body.at("/data/status").asText());
        assertEquals("vehicle-api", body.at("/data/app").asText());
    }

    @Test
    void swaggerDocsAvailable() {
        ResponseEntity<String> response = getFollowingRedirect("/docs");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toLowerCase().contains("swagger"));
    }

    @Test
    void openApiSchemaContainsRequiredEndpoints() {
        JsonNode paths = json(get("/openapi.json")).get("paths");

        assertEquals(
                Set.of("put"), operations(paths.get("/api/v1/vehicles/{vehicle_id}/location")));
        assertEquals(
                Set.of("patch"), operations(paths.get("/api/v1/vehicles/{vehicle_id}/status")));
        assertEquals(Set.of("get"), operations(paths.get("/api/v1/vehicles/{vehicle_id}")));
    }

    @Test
    void unknownRouteReturnsUnifiedError() {
        ResponseEntity<JsonNode> response = get("/api/v1/not-exists");

        assertEquals(404, response.getStatusCode().value());
        JsonNode body = json(response);
        assertEquals(404, body.get("code").asInt());
        assertTrue(body.get("data").isNull());
    }

    @Test
    void methodNotAllowedReturnsUnifiedError() {
        ResponseEntity<JsonNode> response = delete("/api/v1/vehicles/VH-1001");

        assertEquals(405, response.getStatusCode().value());
        assertEquals(405, json(response).get("code").asInt());
    }

    private static Set<String> operations(JsonNode pathItem) {
        assertNotNull(pathItem, "OpenAPI 文档中缺少该路径");
        Set<String> names = new HashSet<>();
        pathItem.properties().forEach(entry -> names.add(entry.getKey()));
        names.remove("parameters");
        return names;
    }
}
