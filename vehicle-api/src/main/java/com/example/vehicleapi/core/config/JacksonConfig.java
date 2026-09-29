package com.example.vehicleapi.core.config;

import com.example.vehicleapi.core.time.UtcInstantDeserializer;
import com.example.vehicleapi.core.time.UtcInstantSerializer;
import java.time.Instant;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 定制：统一 UTC 时间的序列化 / 反序列化行为。
 *
 * <p>对应 Python 版本的 {@code app/core/types.py}。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer utcInstantCustomizer() {
        return builder ->
                builder
                        .serializerByType(Instant.class, new UtcInstantSerializer())
                        .deserializerByType(Instant.class, new UtcInstantDeserializer());
    }
}
