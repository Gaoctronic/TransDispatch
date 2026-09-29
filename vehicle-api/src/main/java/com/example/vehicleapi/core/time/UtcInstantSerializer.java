package com.example.vehicleapi.core.time;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 把 {@link Instant} 序列化为 ISO 8601 字符串（UTC 用 {@code Z} 结尾，保留毫秒）。
 *
 * <p>例如 {@code 2026-09-29T02:15:30.000Z}，与 Python 版本的 {@code format_datetime} 行为一致。
 */
public class UtcInstantSerializer extends JsonSerializer<Instant> {

    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeString(FORMATTER.format(value));
    }
}
