package com.example.vehicleapi.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 应用配置。
 *
 * <p>所有配置项都来自 Spring 属性（默认在 {@code application.yml} 中声明），并可以通过环境变量覆盖，
 * 且都有安全默认值。对应 Python 版本的 {@code app/core/config.py}。
 */
@Component
public class Settings {

    /** 应用描述（固定文案，不通过环境变量覆盖，与 Python 版本一致）。 */
    public static final String DEFAULT_APP_DESCRIPTION =
            "车辆位置与状态管理后端服务（Spring Boot + Bean Validation + 内存存储）。\n\n"
                    + "提供车辆位置的设置/更新、车辆其他状态的局部更新以及车辆信息查询能力。";

    private final String appName;
    private final String appVersion;
    private final String apiPrefix;
    private final String host;
    private final int port;
    private final boolean debug;

    public Settings(
            @Value("${app.name:vehicle-api}") String appName,
            @Value("${app.version:1.0.0}") String appVersion,
            @Value("${api.prefix:/api/v1}") String apiPrefix,
            @Value("${server.address:127.0.0.1}") String host,
            @Value("${server.port:8000}") int port,
            @Value("${app.debug:false}") boolean debug) {
        this.appName = appName;
        this.appVersion = appVersion;
        this.apiPrefix = normalizePrefix(apiPrefix);
        this.host = host;
        this.port = port;
        this.debug = debug;
    }

    private static String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank() || "/".equals(prefix)) {
            return "";
        }
        return prefix.startsWith("/") ? chompTrailingSlash(prefix) : "/" + chompTrailingSlash(prefix);
    }

    private static String chompTrailingSlash(String value) {
        String result = value;
        while (result.length() > 1 && result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getAppDescription() {
        return DEFAULT_APP_DESCRIPTION;
    }

    public String getApiPrefix() {
        return apiPrefix;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isDebug() {
        return debug;
    }
}
