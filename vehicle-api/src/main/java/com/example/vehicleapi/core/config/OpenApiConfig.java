package com.example.vehicleapi.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档信息（标题、版本、描述、标签）。
 *
 * <p>对应 Python 版本在 {@code create_app()} 中传入的 title / version / description / openapi_tags。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vehicleApiOpenApi(Settings settings) {
        return new OpenAPI()
                .info(
                        new Info()
                                .title(settings.getAppName())
                                .version(settings.getAppVersion())
                                .description(settings.getAppDescription())
                                .license(
                                        new License()
                                                .name("MIT")
                                                .url("https://opensource.org/licenses/MIT")))
                .tags(
                        List.of(
                                new Tag().name("车辆").description("车辆位置与状态的写入、查询接口"),
                                new Tag().name("系统").description("健康检查等运维接口")));
    }
}
