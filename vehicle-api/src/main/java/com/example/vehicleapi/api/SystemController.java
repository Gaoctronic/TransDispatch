package com.example.vehicleapi.api;

import com.example.vehicleapi.core.config.Settings;
import com.example.vehicleapi.core.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统 / 运维接口。
 *
 * <ul>
 *   <li>{@code GET /health} 存活探针
 *   <li>{@code GET /redoc}  ReDoc 文档页面
 * </ul>
 */
@RestController
@Tag(name = "系统", description = "健康检查等运维接口")
public class SystemController {

    private static final String REDOC_HTML =
            """
            <!doctype html>
            <html>
              <head>
                <title>vehicle-api · ReDoc</title>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1" />
                <style>body { margin: 0; padding: 0; }</style>
              </head>
              <body>
                <redoc spec-url="/openapi.json"></redoc>
                <script src="https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js"></script>
              </body>
            </html>
            """;

    private final Settings settings;

    public SystemController(Settings settings) {
        this.settings = settings;
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "ok");
        data.put("app", settings.getAppName());
        data.put("version", settings.getAppVersion());
        return ApiResponse.success(data);
    }

    @GetMapping(value = "/redoc", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "ReDoc 文档页面")
    public String redoc() {
        return REDOC_HTML;
    }
}
