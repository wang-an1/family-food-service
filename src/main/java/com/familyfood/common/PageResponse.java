package com.familyfood.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "分页响应")
public record PageResponse<T>(
        @Schema(description = "分页记录")
        List<T> records,
        @Schema(description = "当前页码", example = "1")
        long page,
        @Schema(description = "每页数量", example = "20")
        long pageSize,
        @Schema(description = "总记录数", example = "100")
        long total) {
}
