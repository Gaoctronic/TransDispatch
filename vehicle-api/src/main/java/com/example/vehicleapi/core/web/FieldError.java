package com.example.vehicleapi.core.web;

/**
 * 参数校验失败时，{@code data.errors} 中的单条错误。
 *
 * <p>字段含义与 Python 版本（Pydantic 校验错误）保持一致：
 *
 * <ul>
 *   <li>{@code field}：出错位置，例如 {@code body.latitude}、{@code path.vehicle_id}
 *   <li>{@code type}：错误类型，例如 {@code missing}、{@code less_than_equal}、{@code extra_forbidden}
 *   <li>{@code message}：可读的提示信息
 * </ul>
 */
public record FieldError(String field, String type, String message) {}
