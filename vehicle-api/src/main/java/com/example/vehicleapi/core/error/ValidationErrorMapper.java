package com.example.vehicleapi.core.error;

import com.example.vehicleapi.core.web.FieldError;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.springframework.context.MessageSourceResolvable;

/**
 * 把 Bean Validation 的约束违例转换成与 Python 版本（Pydantic）一致的结构。
 *
 * <p>这里只做尽力对齐：field 与 HTTP 行为完全一致，type 使用 Pydantic 的错误类型命名，
 * message 直接采用约束注解上声明的可读文案。
 */
public final class ValidationErrorMapper {

    /** 已知的约束注解名，按长度从长到短匹配，避免前缀误判。 */
    private static final String[] KNOWN_CONSTRAINTS = {
        "AllowedStatusKeys",
        "PositiveOrZero",
        "NegativeOrZero",
        "DecimalMin",
        "DecimalMax",
        "NotEmpty",
        "NotBlank",
        "NotNull",
        "Pattern",
        "Size",
        "Min",
        "Max"
    };

    private ValidationErrorMapper() {}

    public static FieldError toFieldError(org.springframework.validation.FieldError error) {
        String field = error.getField().isEmpty() ? "body" : "body." + error.getField();
        return new FieldError(field, typeOf(error), messageOf(error));
    }

    static String typeOf(org.springframework.validation.FieldError error) {
        String constraint = constraintName(error);
        if (constraint == null) {
            return "value_error";
        }
        return switch (constraint) {
            case "NotNull", "NotEmpty", "NotBlank" -> "missing";
            case "DecimalMin", "Min", "Positive", "PositiveOrZero" -> "greater_than_equal";
            case "DecimalMax", "Max", "Negative", "NegativeOrZero" -> "less_than_equal";
            case "Size" -> sizeType(error.getRejectedValue());
            case "Pattern" -> "string_pattern_mismatch";
            default -> "value_error";
        };
    }

    /**
     * 从错误码列表里找出对应的约束名。
     *
     * <p>Spring 生成的错误码形如 {@code DecimalMax.vehicleLocation.longitude}、{@code DecimalMax.longitude}、
     * {@code DecimalMax}，因此这里扫描整个数组而不是只取第一个。
     */
    private static String constraintName(org.springframework.validation.FieldError error) {
        String[] codes = error.getCodes();
        if (codes == null) {
            return null;
        }
        return Arrays.stream(codes)
                .map(ValidationErrorMapper::matchKnownConstraint)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private static String matchKnownConstraint(String code) {
        if (code == null) {
            return null;
        }
        for (String known : KNOWN_CONSTRAINTS) {
            if (code.equals(known) || code.startsWith(known + ".")) {
                return known;
            }
        }
        return null;
    }

    private static String sizeType(Object rejectedValue) {
        int length = lengthOf(rejectedValue);
        if (rejectedValue instanceof CharSequence) {
            return length < 1 ? "string_too_short" : "string_too_long";
        }
        return length < 1 ? "too_short" : "too_long";
    }

    private static int lengthOf(Object value) {
        if (value instanceof CharSequence text) {
            return text.length();
        }
        if (value instanceof Collection<?> collection) {
            return collection.size();
        }
        if (value instanceof Map<?, ?> map) {
            return map.size();
        }
        if (value != null && value.getClass().isArray()) {
            return Array.getLength(value);
        }
        return 0;
    }

    public static String messageOf(MessageSourceResolvable error) {
        String message = error.getDefaultMessage();
        return (message == null || message.isBlank()) ? "参数不合法" : message;
    }
}
