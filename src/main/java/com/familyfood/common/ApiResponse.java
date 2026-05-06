package com.familyfood.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "统一 API 响应")
public record ApiResponse<T>(
        @Schema(description = "业务响应码", example = "OK")
        String code,
        @Schema(description = "响应消息", example = "success")
        String message,
        @Schema(description = "响应数据")
        T data,
        @Schema(description = "请求追踪 ID", example = "2f0c7cf5d1d04b91a2d71f5f67d5b4fb")
        String traceId,
        @Schema(description = "字段级错误明细")
        List<ErrorDetail> errors) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("OK", "success", data, null, null);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>("OK", "success", null, null, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return error(code, message, null);
    }

    public static <T> ApiResponse<T> error(String code, String message, List<ErrorDetail> errors) {
        List<ErrorDetail> safeErrors = errors == null || errors.isEmpty() ? null : List.copyOf(errors);
        return new ApiResponse<>(code, message, null, TraceIds.current(), safeErrors);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "字段级错误")
    public record ErrorDetail(
            @Schema(description = "字段名", example = "name")
            String field,
            @Schema(description = "错误信息", example = "不能为空")
            String message) {
    }
}
