package com.example.vehicleapi.core.error;

import com.example.vehicleapi.core.web.FieldError;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.converter.HttpMessageNotReadableException;

/**
 * 把 Jackson 的反序列化错误转换成与 Python 版本（Pydantic）一致的结构。
 *
 * <ul>
 *   <li>未知字段 → {@code extra_forbidden}（等价于 Pydantic 的 {@code extra="forbid"}）
 *   <li>枚举取值非法 → {@code enum}
 *   <li>类型不匹配 → {@code string_type} / {@code list_type} / {@code float_type} 等
 *   <li>JSON 本身不合法 → {@code json_invalid}
 * </ul>
 */
public final class JsonErrorMapper {

    private JsonErrorMapper() {}

    public static FieldError toFieldError(HttpMessageNotReadableException exception) {
        Throwable cause = exception.getMostSpecificCause();

        if (cause instanceof UnrecognizedPropertyException unrecognized) {
            return new FieldError(
                    "body." + unrecognized.getPropertyName(),
                    "extra_forbidden",
                    "Extra inputs are not permitted");
        }
        if (cause instanceof InvalidFormatException invalidFormat) {
            Class<?> targetType = invalidFormat.getTargetType();
            if (targetType != null && targetType.isEnum()) {
                return new FieldError(fieldPath(invalidFormat), "enum", enumMessage(targetType));
            }
            return new FieldError(
                    fieldPath(invalidFormat),
                    typeName(targetType),
                    originalMessage(invalidFormat));
        }
        if (cause instanceof MismatchedInputException mismatched) {
            return new FieldError(
                    fieldPath(mismatched), typeName(mismatched.getTargetType()), originalMessage(mismatched));
        }
        return new FieldError("body", "json_invalid", "JSON decode error");
    }

    private static String fieldPath(JsonMappingException exception) {
        String path =
                exception.getPath().stream()
                        .map(JsonMappingException.Reference::getFieldName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining("."));
        return path.isEmpty() ? "body" : "body." + path;
    }

    private static String originalMessage(JsonMappingException exception) {
        String message = exception.getOriginalMessage();
        return (message == null || message.isBlank()) ? "参数不合法" : message;
    }

    private static String typeName(Class<?> targetType) {
        if (targetType == null) {
            return "value_error";
        }
        if (List.class.isAssignableFrom(targetType)) {
            return "list_type";
        }
        if (Map.class.isAssignableFrom(targetType)) {
            return "dict_type";
        }
        if (String.class == targetType) {
            return "string_type";
        }
        if (Boolean.class == targetType || boolean.class == targetType) {
            return "bool_type";
        }
        if (Integer.class == targetType || int.class == targetType) {
            return "int_type";
        }
        if (Double.class == targetType
                || double.class == targetType
                || Float.class == targetType
                || float.class == targetType) {
            return "float_type";
        }
        if (Instant.class == targetType) {
            return "datetime_parsing";
        }
        if (targetType.isEnum()) {
            return "enum";
        }
        return "value_error";
    }

    private static String enumMessage(Class<?> enumType) {
        Object[] constants = enumType.getEnumConstants();
        if (constants == null || constants.length == 0) {
            return "Input should be a valid value";
        }
        List<String> names = Arrays.stream(constants).map(Object::toString).toList();
        if (names.size() == 1) {
            return "Input should be '" + names.get(0) + "'";
        }
        String head =
                names.subList(0, names.size() - 1).stream()
                        .map(name -> "'" + name + "'")
                        .collect(Collectors.joining(", "));
        return "Input should be " + head + " or '" + names.get(names.size() - 1) + "'";
    }
}
