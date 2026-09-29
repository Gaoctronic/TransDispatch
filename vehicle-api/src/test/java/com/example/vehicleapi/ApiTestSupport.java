package com.example.vehicleapi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.vehicleapi.service.InMemoryVehicleStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * 测试基类：真实启动一次应用（随机端口），并通过 HTTP 访问接口。
 *
 * <p>对应 Python 版本的 {@code tests/conftest.py}：每个用例开始前都会清空内存仓库，
 * 保证用例之间互不干扰。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ApiTestSupport {

    protected static final String API = "/api/v1";

    @Autowired protected TestRestTemplate restTemplate;

    @Autowired protected ObjectMapper objectMapper;

    @Autowired protected InMemoryVehicleStore store;

    @BeforeEach
    void resetStore() {
        store.clear();
    }

    protected ResponseEntity<JsonNode> exchange(HttpMethod method, String url, Object payload) {
        return restTemplate.exchange(url, method, jsonEntity(payload), JsonNode.class);
    }

    protected ResponseEntity<JsonNode> put(String url, Object payload) {
        return exchange(HttpMethod.PUT, url, payload);
    }

    protected ResponseEntity<JsonNode> patch(String url, Object payload) {
        return exchange(HttpMethod.PATCH, url, payload);
    }

    /** 发送原始文本请求体（用于验证非法 JSON 的处理）。 */
    protected ResponseEntity<JsonNode> patchRaw(String url, String rawBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<byte[]> entity = new HttpEntity<>(rawBody.getBytes(StandardCharsets.UTF_8), headers);
        return restTemplate.exchange(url, HttpMethod.PATCH, entity, JsonNode.class);
    }

    protected ResponseEntity<JsonNode> get(String url, Object... uriVariables) {
        return restTemplate.exchange(url, HttpMethod.GET, null, JsonNode.class, uriVariables);
    }

    protected ResponseEntity<JsonNode> delete(String url) {
        return restTemplate.exchange(url, HttpMethod.DELETE, null, JsonNode.class);
    }

    /**
     * GET 请求，并在遇到 3xx 时手动跟随一次重定向（与浏览器行为一致）。
     *
     * <p>用于访问 {@code /docs} 这类可能返回重定向的文档地址。
     */
    protected ResponseEntity<String> getFollowingRedirect(String url) {
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
        if (response.getStatusCode().is3xxRedirection() && location != null) {
            URI target = URI.create(restTemplate.getRootUri()).resolve(location);
            response = restTemplate.getForEntity(target, String.class);
        }
        return response;
    }

    protected JsonNode json(ResponseEntity<JsonNode> response) {
        JsonNode body = response.getBody();
        assertNotNull(body, "响应体不应为空");
        return body;
    }

    protected JsonNode tree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("测试用例里的 JSON 字面量不合法", exception);
        }
    }

    /** 收集 422 响应里 {@code data.errors} 的 field 列表。 */
    protected static List<String> errorFields(JsonNode body) {
        List<String> fields = new ArrayList<>();
        for (JsonNode error : body.at("/data/errors")) {
            fields.add(error.get("field").asText());
        }
        return fields;
    }

    protected static HttpEntity<Object> jsonEntity(Object payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(payload, headers);
    }

    /** 便捷构造有序 Map（模拟 Python 的 dict 字面量）。 */
    protected static Map<String, Object> map(Object... keyValues) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            result.put((String) keyValues[i], keyValues[i + 1]);
        }
        return result;
    }
}
