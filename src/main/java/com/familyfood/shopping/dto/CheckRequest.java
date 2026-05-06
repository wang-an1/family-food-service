package com.familyfood.shopping.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "采购条目勾选请求")
public record CheckRequest(
        @Schema(description = "是否已购买", example = "true")
        boolean checked) {
}
