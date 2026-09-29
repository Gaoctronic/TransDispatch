package com.example.vehicleapi.core.time;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 把 ISO 8601 字符串解析为 {@link Instant}。
 *
 * <p>与 Python 版本保持一致：不带时区的时间按 UTC 处理；数字按 Unix 秒处理。
 */
public class UtcInstantDeserializer extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_NUMBER_INT) {
            return Instant.ofEpochMilli(Math.multiplyExact(parser.getLongValue(), 1000L));
        }
        if (token == JsonToken.VALUE_NUMBER_FLOAT) {
            return Instant.ofEpochMilli((long) (parser.getDoubleValue() * 1000L));
        }

        String raw = parser.getValueAsString();
        if (raw == null) {
            return (Instant) context.handleUnexpectedToken(Instant.class, parser);
        }
        Instant parsed = tryParse(raw.trim());
        if (parsed == null) {
            return (Instant)
                    context.reportInputMismatch(
                            Instant.class, "无法解析时间：%s（请使用 ISO 8601 格式）", raw);
        }
        return parsed;
    }

    private static Instant tryParse(String value) {
        try {
            return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
        } catch (DateTimeParseException ignored) {
            // 继续尝试其他格式
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            // 继续尝试其他格式
        }
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            // 继续尝试其他格式
        }
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
