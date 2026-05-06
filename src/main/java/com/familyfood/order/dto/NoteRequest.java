package com.familyfood.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "订单备注或原因请求")
public record NoteRequest(
        @Schema(description = "确认备注", example = "已确认")
        @Size(max = 500)
        String note,
        @Schema(description = "取消原因", example = "临时取消")
        @Size(max = 500)
        String reason) {
}
